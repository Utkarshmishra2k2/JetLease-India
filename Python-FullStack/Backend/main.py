from __future__ import annotations
from datetime import date, datetime
from math import ceil, sin, cos, atan2, sqrt
from threading import Lock
from typing import Optional, List, Any
import csv, io, os, re, uuid

from fastapi import FastAPI, Request, Query, Body, HTTPException, Depends
from fastapi.openapi.utils import get_openapi
from fastapi.responses import PlainTextResponse, Response
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sqlalchemy import create_engine, String, Integer, Float, Boolean, BigInteger, Text, select, func, or_, UniqueConstraint
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, sessionmaker, Session
from sqlalchemy.exc import IntegrityError

# ---------------- configuration ----------------
PORT = int(os.getenv('PORT', '8080'))
DATABASE_URL = os.getenv('DATABASE_URL', 'sqlite:///./data/jetlease.db')
ALLOWED_ORIGINS = [x.strip() for x in os.getenv('JETLEASE_CORS_ALLOWED_ORIGINS', 'http://localhost:4200').split(',') if x.strip()]
SEED_ENABLED = os.getenv('JETLEASE_SEED_ENABLED', 'true').lower() != 'false'

if DATABASE_URL.startswith('sqlite:///'):
    os.makedirs(os.path.dirname(DATABASE_URL.replace('sqlite:///', '', 1)) or '.', exist_ok=True)
engine = create_engine(DATABASE_URL, connect_args={'check_same_thread': False} if DATABASE_URL.startswith('sqlite') else {})
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False, expire_on_commit=False)

class Base(DeclarativeBase): pass

# ---------------- entities / database schema ----------------
class AadhaarRegistry(Base):
    __tablename__='aadhaar_registry'; __table_args__=(UniqueConstraint('aadhaar_number'),)
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    aadhaarNumber: Mapped[Optional[str]] = mapped_column('aadhaar_number', String, unique=True)
    holderName: Mapped[Optional[str]] = mapped_column('holder_name', String)
    dob: Mapped[Optional[str]] = mapped_column(String); gender: Mapped[Optional[str]] = mapped_column(String); status: Mapped[Optional[str]] = mapped_column(String)
class Aircraft(Base):
    __tablename__='aircraft'
    id: Mapped[str] = mapped_column(String, primary_key=True); reg: Mapped[Optional[str]]=mapped_column(String); model: Mapped[Optional[str]]=mapped_column(String); manufacturer: Mapped[Optional[str]]=mapped_column(String); category: Mapped[Optional[str]]=mapped_column(String); capacity: Mapped[int]=mapped_column(Integer,default=0); speed: Mapped[int]=mapped_column(Integer,default=0); rangeKm: Mapped[int]=mapped_column('range_km',Integer,default=0); hourlyRate: Mapped[int]=mapped_column('hourly_rate',BigInteger,default=0); status: Mapped[Optional[str]]=mapped_column(String); typeRating: Mapped[Optional[str]]=mapped_column('type_rating',String)
class AuditLog(Base):
    __tablename__='audit_log'
    id: Mapped[str]=mapped_column(String,primary_key=True); actor: Mapped[Optional[str]]=mapped_column(String); category: Mapped[Optional[str]]=mapped_column(String); action: Mapped[Optional[str]]=mapped_column(String); details: Mapped[Optional[str]]=mapped_column(String(2000)); timestamp: Mapped[Optional[str]]=mapped_column(String)
class BankLedger(Base):
    __tablename__='bank_ledger'
    id: Mapped[int]=mapped_column(Integer,primary_key=True,autoincrement=True); transactionId: Mapped[Optional[str]]=mapped_column('transaction_id',String); bookingId: Mapped[Optional[str]]=mapped_column('booking_id',String); amount: Mapped[int]=mapped_column(BigInteger,default=0); status: Mapped[Optional[str]]=mapped_column(String); clearedAt: Mapped[Optional[str]]=mapped_column('cleared_at',String)
class Booking(Base):
    __tablename__='bookings'
    id: Mapped[str]=mapped_column(String,primary_key=True); userEmail: Mapped[Optional[str]]=mapped_column('user_email',String); type: Mapped[Optional[str]]=mapped_column(String); tripType: Mapped[Optional[str]]=mapped_column('trip_type',String); origin: Mapped[Optional[str]]=mapped_column(String); destination: Mapped[Optional[str]]=mapped_column(String); date_: Mapped[Optional[str]]=mapped_column('date',String); time: Mapped[Optional[str]]=mapped_column(String); returnDate: Mapped[Optional[str]]=mapped_column('return_date',String); returnTime: Mapped[Optional[str]]=mapped_column('return_time',String); pax: Mapped[int]=mapped_column(Integer,default=0); aircraftId: Mapped[Optional[str]]=mapped_column('aircraft_id',String); aircraftModel: Mapped[Optional[str]]=mapped_column('aircraft_model',String); selfFly: Mapped[bool]=mapped_column('self_fly',Boolean,default=False); licenseNumber: Mapped[Optional[str]]=mapped_column('license_number',String); licenseClass: Mapped[Optional[str]]=mapped_column('license_class',String); flyingHours: Mapped[int]=mapped_column('flying_hours',Integer,default=0); certificateFileName: Mapped[Optional[str]]=mapped_column('certificate_file_name',String); dgcaDeclaration: Mapped[bool]=mapped_column('dgca_declaration',Boolean,default=False); licenseVerified: Mapped[bool]=mapped_column('license_verified',Boolean,default=False); hours: Mapped[float]=mapped_column(Float,default=0); aircraftCost: Mapped[int]=mapped_column('aircraft_cost',BigInteger,default=0); pilotCost: Mapped[int]=mapped_column('pilot_cost',BigInteger,default=0); crewCost: Mapped[int]=mapped_column('crew_cost',BigInteger,default=0); airportCharges: Mapped[int]=mapped_column('airport_charges',BigInteger,default=0); fuelSurcharge: Mapped[int]=mapped_column('fuel_surcharge',BigInteger,default=0); gst: Mapped[int]=mapped_column(BigInteger,default=0); total: Mapped[int]=mapped_column(BigInteger,default=0); status: Mapped[Optional[str]]=mapped_column(String); assignedPilotId: Mapped[Optional[str]]=mapped_column('assigned_pilot_id',String); assignedCrewIds: Mapped[Optional[str]]=mapped_column('assigned_crew_ids',String); createdAt: Mapped[Optional[str]]=mapped_column('created_at',String)
class ContactMessage(Base):
    __tablename__='contact_messages'
    id: Mapped[str]=mapped_column(String,primary_key=True); name: Mapped[Optional[str]]=mapped_column(String); phone: Mapped[Optional[str]]=mapped_column(String); email: Mapped[Optional[str]]=mapped_column(String); message: Mapped[Optional[str]]=mapped_column(String(2000)); status: Mapped[Optional[str]]=mapped_column(String); createdAt: Mapped[Optional[str]]=mapped_column('created_at',String)
class Crew(Base):
    __tablename__='crew'
    id: Mapped[str]=mapped_column(String,primary_key=True); name: Mapped[Optional[str]]=mapped_column(String); role: Mapped[Optional[str]]=mapped_column(String); remainingHours: Mapped[float]=mapped_column('remaining_hours',Float,default=0); available: Mapped[bool]=mapped_column(Boolean,default=False)
class Faq(Base):
    __tablename__='faq'
    id: Mapped[int]=mapped_column(Integer,primary_key=True,autoincrement=True); question: Mapped[Optional[str]]=mapped_column(String); answer: Mapped[Optional[str]]=mapped_column(String(2000))
class Lease(Base):
    __tablename__='leases'
    id: Mapped[str]=mapped_column(String,primary_key=True); bookingId: Mapped[Optional[str]]=mapped_column('booking_id',String); userEmail: Mapped[Optional[str]]=mapped_column('user_email',String); status: Mapped[Optional[str]]=mapped_column(String); signedBy: Mapped[Optional[str]]=mapped_column('signed_by',String); signedDate: Mapped[Optional[str]]=mapped_column('signed_date',String); approvalDate: Mapped[Optional[str]]=mapped_column('approval_date',String); createdAt: Mapped[Optional[str]]=mapped_column('created_at',String)
class Notification(Base):
    __tablename__='notifications'
    id: Mapped[str]=mapped_column(String,primary_key=True); userEmail: Mapped[Optional[str]]=mapped_column('user_email',String); title: Mapped[Optional[str]]=mapped_column(String); message: Mapped[Optional[str]]=mapped_column(String(2000)); type: Mapped[Optional[str]]=mapped_column(String); read: Mapped[bool]=mapped_column('is_read',Boolean,default=False); createdAt: Mapped[Optional[str]]=mapped_column('created_at',String)
class Passenger(Base):
    __tablename__='passengers'
    id: Mapped[int]=mapped_column(Integer,primary_key=True,autoincrement=True); bookingId: Mapped[Optional[str]]=mapped_column('booking_id',String); name: Mapped[Optional[str]]=mapped_column(String); dob: Mapped[Optional[str]]=mapped_column(String); gender: Mapped[Optional[str]]=mapped_column(String); aadhaar: Mapped[Optional[str]]=mapped_column(String); verificationStatus: Mapped[Optional[str]]=mapped_column('verification_status',String); noAadhaar: Mapped[bool]=mapped_column('no_aadhaar',Boolean,default=False); altDocumentId: Mapped[Optional[str]]=mapped_column('alt_document_id',String)
class Payment(Base):
    __tablename__='payments'
    id: Mapped[str]=mapped_column(String,primary_key=True); bookingId: Mapped[Optional[str]]=mapped_column('booking_id',String); userEmail: Mapped[Optional[str]]=mapped_column('user_email',String); amount: Mapped[int]=mapped_column(BigInteger,default=0); transactionId: Mapped[Optional[str]]=mapped_column('transaction_id',String); status: Mapped[Optional[str]]=mapped_column(String); submittedAt: Mapped[Optional[str]]=mapped_column('submitted_at',String); cancellationFee: Mapped[int]=mapped_column('cancellation_fee',BigInteger,default=0); refundAmount: Mapped[int]=mapped_column('refund_amount',BigInteger,default=0)
class Pilot(Base):
    __tablename__='pilots'
    id: Mapped[str]=mapped_column(String,primary_key=True); name: Mapped[Optional[str]]=mapped_column(String); licenseNumber: Mapped[Optional[str]]=mapped_column('license_number',String); remainingHours: Mapped[float]=mapped_column('remaining_hours',Float,default=0); available: Mapped[bool]=mapped_column(Boolean,default=False)
class PilotLicenseRegistry(Base):
    __tablename__='pilot_license_registry'; __table_args__=(UniqueConstraint('license_number'),)
    id: Mapped[int]=mapped_column(Integer,primary_key=True,autoincrement=True); licenseNumber: Mapped[Optional[str]]=mapped_column('license_number',String,unique=True); holderName: Mapped[Optional[str]]=mapped_column('holder_name',String); licenseClass: Mapped[Optional[str]]=mapped_column('license_class',String); hoursOnRecord: Mapped[int]=mapped_column('hours_on_record',Integer,default=0); status: Mapped[Optional[str]]=mapped_column(String)
class Report(Base):
    __tablename__='reports'
    id: Mapped[str]=mapped_column(String,primary_key=True); bookingId: Mapped[Optional[str]]=mapped_column('booking_id',String); userEmail: Mapped[Optional[str]]=mapped_column('user_email',String); subject: Mapped[Optional[str]]=mapped_column(String); details: Mapped[Optional[str]]=mapped_column(String(2000)); status: Mapped[Optional[str]]=mapped_column(String); createdAt: Mapped[Optional[str]]=mapped_column('created_at',String)
class Route(Base):
    __tablename__='routes'
    code: Mapped[str]=mapped_column(String,primary_key=True); city: Mapped[Optional[str]]=mapped_column(String); airport: Mapped[Optional[str]]=mapped_column(String); lat: Mapped[float]=mapped_column(Float,default=0); lon: Mapped[float]=mapped_column(Float,default=0)
class Testimonial(Base):
    __tablename__='testimonials'
    id: Mapped[int]=mapped_column(Integer,primary_key=True,autoincrement=True); name: Mapped[Optional[str]]=mapped_column(String); role: Mapped[Optional[str]]=mapped_column(String); quote: Mapped[Optional[str]]=mapped_column(String(1000)); rating: Mapped[int]=mapped_column(Integer,default=0)
class User(Base):
    __tablename__='users'
    id: Mapped[str]=mapped_column(String,primary_key=True); fullName: Mapped[str]=mapped_column('full_name',String,nullable=False); email: Mapped[str]=mapped_column(String,nullable=False,unique=True); phone: Mapped[str]=mapped_column(String,nullable=False); dob: Mapped[Optional[str]]=mapped_column(String); emergencyContact: Mapped[Optional[str]]=mapped_column('emergency_contact',String); password: Mapped[str]=mapped_column(String,nullable=False); country: Mapped[Optional[str]]=mapped_column(String); role: Mapped[str]=mapped_column(String,nullable=False); status: Mapped[str]=mapped_column(String,nullable=False); membership: Mapped[Optional[str]]=mapped_column(String); loyaltyPoints: Mapped[int]=mapped_column('loyalty_points',Integer,default=0); createdAt: Mapped[Optional[str]]=mapped_column('created_at',String)

# ---------------- DTOs ----------------
class AddAircraftRequest(BaseModel): reg: Optional[str]=None; model: Optional[str]=None; manufacturer: Optional[str]=None; category: Optional[str]=None; capacity:int=0; speed:int=0; rangeKm:int=0; hourlyRate:int=0; typeRating:Optional[str]=None
class AdminLoginRequest(BaseModel): email:Optional[str]=None; password:Optional[str]=None
class AssignCrewRequest(BaseModel): pilotId:Optional[str]=None; crewIds:Optional[List[str]]=None
class ChangePhoneRequest(BaseModel): newPhone:Optional[str]=None; otp:Optional[str]=None
class ContactRequest(BaseModel): name:Optional[str]=None; phone:Optional[str]=None; email:Optional[str]=None; message:Optional[str]=None
class PassengerRequest(BaseModel): name:Optional[str]=None; dob:Optional[str]=None; gender:Optional[str]=None; aadhaar:Optional[str]=None; verificationStatus:Optional[str]=None; noAadhaar:bool=False; altDocumentId:Optional[str]=None
class SelfFlyRequest(BaseModel): licenseNumber:Optional[str]=None; licenseClass:Optional[str]=None; flyingHours:int=0; dgcaDeclaration:bool=False; verified:bool=False; certificateFileName:Optional[str]=None
class CreateBookingRequest(BaseModel): type:Optional[str]=None; tripType:Optional[str]=None; origin:Optional[str]=None; destination:Optional[str]=None; date:Optional[str]=None; time:Optional[str]=None; returnDate:Optional[str]=None; returnTime:Optional[str]=None; pax:int=0; aircraftId:Optional[str]=None; selfFly:bool=False; selfFlyDetails:Optional[SelfFlyRequest]=None; passengers:Optional[List[PassengerRequest]]=None
class ForgotPasswordRequest(BaseModel): email:Optional[str]=None
class LoginRequest(BaseModel): identifierType:Optional[str]=None; identifier:Optional[str]=None; password:Optional[str]=None
class PayRequest(BaseModel): transactionId:Optional[str]=None
class RateRequest(BaseModel): hourlyRate:int=0
class RecommendRequest(BaseModel): pax:int=0; budget:int=0; distanceKm:int=0; category:Optional[str]=None
class RegisterRequest(BaseModel): fullName:Optional[str]=None; email:Optional[str]=None; phone:Optional[str]=None; dob:Optional[str]=None; emergencyContact:Optional[str]=None; password:Optional[str]=None; confirmPassword:Optional[str]=None
class ReportIssueRequest(BaseModel): bookingId:Optional[str]=None; subject:Optional[str]=None; details:Optional[str]=None
class ResetPasswordRequest(BaseModel): email:Optional[str]=None; otp:Optional[str]=None; newPassword:Optional[str]=None; confirmPassword:Optional[str]=None
class SignLeaseRequest(BaseModel): legalName:Optional[str]=None
class StatusRequest(BaseModel): status:Optional[str]=None
class UpdateProfileRequest(BaseModel): fullName:Optional[str]=None; dob:Optional[str]=None; emergencyContact:Optional[str]=None
class VerifyAadhaarRequest(BaseModel): aadhaar:Optional[str]=None
class VerifyLicenseRequest(BaseModel): licenseNumber:Optional[str]=None

# ---------------- errors / utilities ----------------
class ApiError(Exception):
    def __init__(self,status:int,message:str): self.status=status; self.message=message
class BadRequestException(ApiError):
    def __init__(self,m): super().__init__(400,m)
class ForbiddenException(ApiError):
    def __init__(self,m): super().__init__(403,m)
class NotFoundException(ApiError):
    def __init__(self,m): super().__init__(404,m)
class UnauthorizedException(ApiError):
    def __init__(self,m): super().__init__(401,m)

def now_iso(): return datetime.now().isoformat()
def today_iso(): return date.today().isoformat()
_counter=int(datetime.now().timestamp()*1000)%100000; _lock=Lock()
def uid(prefix):
    global _counter
    with _lock: _counter+=1; return f'{prefix}-{_counter}'

def name(v):
    if v is None or not v.strip(): return 'This field is required.'
    if not re.fullmatch(r"[A-Za-z][A-Za-z\s.'-]{1,49}",v.strip()): return 'Only letters are allowed.'
    return ''
def email(v):
    if v is None or not v.strip(): return 'Email is required.'
    if not re.fullmatch(r'[^\s@]+@[^\s@]+\.[^\s@]+',v.strip()): return 'Enter a valid email address.'
    return ''
def phone10(v):
    if v is None or not v.strip(): return 'Phone number is required.'
    if not re.fullmatch(r'[0-9]{10}',v.strip()): return 'Enter a valid 10-digit phone number (numbers only).'
    return ''
def aadhaar(v):
    if v is None or not v.strip(): return 'Aadhaar number is required.'
    if not re.fullmatch(r'[0-9]{12}',v.strip()): return 'Aadhaar number must be exactly 12 digits.'
    return ''
def license_number(v):
    if v is None or not v.strip(): return 'License number is required.'
    if not re.fullmatch(r'[A-Za-z0-9-]{4,20}',v.strip()): return 'Enter a valid license number (letters, numbers, hyphens only).'
    return ''
def message(v):
    if v is None or not v.strip(): return 'Message is required.'
    if len(v.strip())<10: return 'Message must be at least 10 characters.'
    return ''
def dob(v):
    if v is None or v=='': return 'Date of birth is required.'
    try: d=date.fromisoformat(v)
    except: return 'Enter a valid date (yyyy-MM-dd).'
    t=date.today()
    if d < t.replace(year=t.year-100): return 'Age cannot be more than 100 years.'
    if d > date.fromordinal(t.toordinal()-15): return 'Passenger must be at least 15 days old - future dates are not allowed.'
    return ''
def is_adult(v):
    try:
        d=date.fromisoformat(v); t=date.today();
        return d<=t and (t.year-d.year-((t.month,t.day)<(d.month,d.day)))>=18
    except: return False

# ---------------- serialization ----------------
def obj(x):
    if x is None:return None
    if isinstance(x, Booking): return {'id':x.id,'userEmail':x.userEmail,'type':x.type,'tripType':x.tripType,'origin':x.origin,'destination':x.destination,'date':x.date_,'time':x.time,'returnDate':x.returnDate,'returnTime':x.returnTime,'pax':x.pax,'aircraftId':x.aircraftId,'aircraftModel':x.aircraftModel,'selfFly':x.selfFly,'licenseNumber':x.licenseNumber,'licenseClass':x.licenseClass,'flyingHours':x.flyingHours,'certificateFileName':x.certificateFileName,'dgcaDeclaration':x.dgcaDeclaration,'licenseVerified':x.licenseVerified,'hours':x.hours,'aircraftCost':x.aircraftCost,'pilotCost':x.pilotCost,'crewCost':x.crewCost,'airportCharges':x.airportCharges,'fuelSurcharge':x.fuelSurcharge,'gst':x.gst,'total':x.total,'status':x.status,'assignedPilotId':x.assignedPilotId,'assignedCrewIds':x.assignedCrewIds,'createdAt':x.createdAt}
    if isinstance(x, User): return {'id':x.id,'fullName':x.fullName,'email':x.email,'phone':x.phone,'dob':x.dob,'emergencyContact':x.emergencyContact,'password':x.password,'country':x.country,'role':x.role,'status':x.status,'membership':x.membership,'loyaltyPoints':x.loyaltyPoints,'createdAt':x.createdAt}
    if isinstance(x, Aircraft): return {'id':x.id,'reg':x.reg,'model':x.model,'manufacturer':x.manufacturer,'category':x.category,'capacity':x.capacity,'speed':x.speed,'rangeKm':x.rangeKm,'hourlyRate':x.hourlyRate,'status':x.status,'typeRating':x.typeRating}
    if isinstance(x, AuditLog): return {'id':x.id,'actor':x.actor,'category':x.category,'action':x.action,'details':x.details,'timestamp':x.timestamp}
    if isinstance(x, BankLedger): return {'id':x.id,'transactionId':x.transactionId,'bookingId':x.bookingId,'amount':x.amount,'status':x.status,'clearedAt':x.clearedAt}
    if isinstance(x, ContactMessage): return {'id':x.id,'name':x.name,'phone':x.phone,'email':x.email,'message':x.message,'status':x.status,'createdAt':x.createdAt}
    if isinstance(x, Crew): return {'id':x.id,'name':x.name,'role':x.role,'remainingHours':x.remainingHours,'available':x.available}
    if isinstance(x, Faq): return {'id':x.id,'question':x.question,'answer':x.answer}
    if isinstance(x, Lease): return {'id':x.id,'bookingId':x.bookingId,'userEmail':x.userEmail,'status':x.status,'signedBy':x.signedBy,'signedDate':x.signedDate,'approvalDate':x.approvalDate,'createdAt':x.createdAt}
    if isinstance(x, Notification): return {'id':x.id,'userEmail':x.userEmail,'title':x.title,'message':x.message,'type':x.type,'read':x.read,'createdAt':x.createdAt}
    if isinstance(x, Passenger): return {'id':x.id,'bookingId':x.bookingId,'name':x.name,'dob':x.dob,'gender':x.gender,'aadhaar':x.aadhaar,'verificationStatus':x.verificationStatus,'noAadhaar':x.noAadhaar,'altDocumentId':x.altDocumentId}
    if isinstance(x, Payment): return {'id':x.id,'bookingId':x.bookingId,'userEmail':x.userEmail,'amount':x.amount,'transactionId':x.transactionId,'status':x.status,'submittedAt':x.submittedAt,'cancellationFee':x.cancellationFee,'refundAmount':x.refundAmount}
    if isinstance(x, Pilot): return {'id':x.id,'name':x.name,'licenseNumber':x.licenseNumber,'remainingHours':x.remainingHours,'available':x.available}
    if isinstance(x, PilotLicenseRegistry): return {'id':x.id,'licenseNumber':x.licenseNumber,'holderName':x.holderName,'licenseClass':x.licenseClass,'hoursOnRecord':x.hoursOnRecord,'status':x.status}
    if isinstance(x, Report): return {'id':x.id,'bookingId':x.bookingId,'userEmail':x.userEmail,'subject':x.subject,'details':x.details,'status':x.status,'createdAt':x.createdAt}
    if isinstance(x, Route): return {'code':x.code,'city':x.city,'airport':x.airport,'lat':x.lat,'lon':x.lon}
    if isinstance(x, Testimonial): return {'id':x.id,'name':x.name,'role':x.role,'quote':x.quote,'rating':x.rating}
    if isinstance(x, dict): return {k:obj(v) for k,v in x.items()}
    if isinstance(x,list): return [obj(v) for v in x]
    return x

# ---------------- services ----------------
def db():
    s=SessionLocal()
    try: yield s
    finally: s.close()

def require_auth(request:Request, dbs:Session=Depends(db), role=None):
    h=request.headers.get('Authorization')
    token=h[7:].strip() if h and h.startswith('Bearer ') else None
    info=sessions.get(token) if token else None
    if not info: raise UnauthorizedException('Session expired or invalid. Please log in again.')
    if role=='customer' and info['role']!='customer': raise ForbiddenException('Customer account required.')
    if role=='admin' and info['role']!='admin': raise ForbiddenException('Admin account required.')
    return info

sessions={}

def audit(dbs, actor, category, action, details):
    dbs.add(AuditLog(id=uid('AUD'),actor=actor,category=category,action=action,details=details,timestamp=now_iso())); dbs.commit()
def notify(dbs,email_,title,msg,type_):
    dbs.add(Notification(id=uid('NTF'),userEmail=email_,title=title,message=msg,type=type_,read=False,createdAt=now_iso())); dbs.commit()

def find_one(dbs, cls, ident, msg):
    x=dbs.get(cls,ident)
    if x is None: raise NotFoundException(msg)
    return x

def haversine(lat1,lon1,lat2,lon2):
    R=6371; dlat=__import__('math').radians(lat2-lat1); dlon=__import__('math').radians(lon2-lon1)
    a=sin(dlat/2)**2+cos(__import__('math').radians(lat1))*cos(__import__('math').radians(lat2))*sin(dlon/2)**2
    return R*2*atan2(sqrt(a),sqrt(1-a))
def distance_km(dbs,o,d):
    ro,rd=dbs.get(Route,o),dbs.get(Route,d)
    return -1 if ro is None or rd is None else round(haversine(ro.lat,ro.lon,rd.lat,rd.lon))
def estimate_hours(distance,speed,round_trip):
    one=distance*2.0 if round_trip else distance; hours=one/speed; hours=ceil(hours*4)/4.0; return max(hours,1.0)
def jround(x): return int(__import__('math').floor(x + 0.5))
def cost_calc(rate,booking_type,trip_type,self_fly,distance,speed):
    rt=trip_type=='Round Trip'; hours=estimate_hours(distance,speed,rt) if distance>0 else (4.0 if rt else 2.0)
    aircraft=jround(rate*hours); pilot=jround((45000*hours)/2.0) if self_fly else jround(45000*hours); crew=jround(12000*2*hours); airport=18000 if booking_type=='Helicopter Charter' else 35000; sub=aircraft+pilot+crew+airport; fuel=jround(sub*.08); gst=jround((sub+fuel)*.05); total=sub+fuel+gst
    return {'hours':hours,'aircraftCost':aircraft,'pilotCost':pilot,'crewCost':crew,'airportCharges':airport,'fuelSurcharge':fuel,'gst':gst,'total':total}

def release_resources(dbs,b):
    a=dbs.get(Aircraft,b.aircraftId)
    if a and a.status=='Booked': a.status='Available'
    if b.assignedPilotId:
        p=dbs.get(Pilot,b.assignedPilotId)
        if p:p.remainingHours+=b.hours
    if b.assignedCrewIds:
        for cid in b.assignedCrewIds.split(','):
            c=dbs.get(Crew,cid.strip())
            if c:c.remainingHours+=b.hours
    dbs.commit()
def void_unsigned_lease(dbs,bid):
    l=dbs.execute(select(Lease).where(Lease.bookingId==bid)).scalar_one_or_none()
    if l and l.status not in ('Signed','Approved'): l.status='Rejected'; dbs.commit()
def ensure_lease(dbs,bid,email_):
    if dbs.execute(select(Lease).where(Lease.bookingId==bid)).scalar_one_or_none() is None:
        dbs.add(Lease(id=uid('LSE'),bookingId=bid,userEmail=email_,status='Sent',createdAt=now_iso())); dbs.commit()

def lease_text(l,b):
    s=f'AIRCRAFT LEASE AGREEMENT\nLease ID: {l.id}\nBooking Reference: {b.id}\nLessee: {l.userEmail}\nAircraft: {b.aircraftModel}\nRoute: {b.origin} to {b.destination}\nDate of Flight: {b.date_}\nTotal Charter Value: INR {b.total}\n\nThis agreement confirms the terms under which JetLease India Charters Pvt Ltd\nleases the above aircraft to the lessee for the stated route and date, subject\nto all applicable DGCA regulations and the JetLease Terms of Service.\n\nStatus: {l.status}\n'
    if l.signedBy is not None:s+=f'Signed By: {l.signedBy} on {l.signedDate}\n'
    return s

# ---------------- app / exception handling ----------------
app=FastAPI(
    title='JetLease India',
    version='1.0.0',
    description='JetLease India charter booking API',
    docs_url='/docs',
    redoc_url='/redoc',
    openapi_url='/openapi.json',
)


def custom_openapi():
    """Generate OpenAPI with the same Bearer session scheme used by require_auth().

    Authentication remains the existing in-memory Bearer-token session mechanism;
    this only makes the requirement visible and usable from Swagger UI.
    """
    if app.openapi_schema:
        return app.openapi_schema

    schema = get_openapi(
        title='JetLease India',
        version='1.0.0',
        description='JetLease India charter booking API',
        routes=app.routes,
    )

    components = schema.setdefault('components', {})
    components['securitySchemes'] = {
        'BearerAuth': {
            'type': 'http',
            'scheme': 'bearer',
            'bearerFormat': 'Token',
            'description': 'Enter the session token returned by the login endpoint. Swagger adds the Bearer prefix automatically.',
        }
    }

    # Endpoints that call require_auth() directly. Keeping this list explicit
    # avoids incorrectly marking public guest/authentication endpoints as secure.
    protected_paths = {
        '/api/auth/logout',
        '/api/auth/session',
        '/api/aircraft',
        '/api/aircraft/available',
        '/api/bookings/verify-aadhaar',
        '/api/bookings/verify-license',
        '/api/bookings',
        '/api/bookings/my',
        '/api/bookings/{id}',
        '/api/bookings/{id}/passengers',
        '/api/bookings/{id}/cancel',
        '/api/payments/my',
        '/api/payments/booking/{bookingId}',
        '/api/payments/booking/{bookingId}/pay',
        '/api/leases/my',
        '/api/leases/{id}',
        '/api/leases/{id}/sign',
        '/api/leases/{id}/export',
        '/api/notifications/my',
        '/api/notifications/mark-read',
        '/api/profile',
        '/api/profile/phone',
        '/api/reports',
    }

    for path, operations in schema.get('paths', {}).items():
        is_protected = path in protected_paths or path.startswith('/api/admin/')
        if not is_protected:
            continue
        for method, operation in operations.items():
            if method in {'get', 'post', 'put', 'patch', 'delete', 'options', 'head'}:
                operation['security'] = [{'BearerAuth': []}]

    app.openapi_schema = schema
    return schema


app.openapi = custom_openapi
app.add_middleware(CORSMiddleware,allow_origins=ALLOWED_ORIGINS,allow_credentials=True,allow_methods=['GET','POST','PUT','PATCH','DELETE','OPTIONS'],allow_headers=['*'],expose_headers=['Authorization'],max_age=3600)
@app.exception_handler(ApiError)
def api_error_handler(request,exc): return Response(content=__import__('json').dumps({'status':exc.status,'message':exc.message,'timestamp':datetime.utcnow().isoformat()+'Z'}),status_code=exc.status,media_type='application/json')

# ---------------- endpoints ----------------
@app.post('/api/auth/register')
def register(req:RegisterRequest,dbs:Session=Depends(db)):
    for field,msg in [('fullName',name(req.fullName)),('email',email(req.email)),('phone',phone10(req.phone))]:
        if msg: raise BadRequestException(msg)
    if dbs.execute(select(User).where(func.lower(User.email)==(req.email or '').lower())).scalar_one_or_none(): raise BadRequestException('An account with this email already exists.')
    e=dob(req.dob)
    if e: raise BadRequestException(e)
    if not is_adult(req.dob): raise BadRequestException('You must be 18 or older.')
    e=phone10(req.emergencyContact)
    if e: raise BadRequestException(e)
    if req.password is None or len(req.password)<8: raise BadRequestException('Password must be at least 8 characters.')
    if req.password!=req.confirmPassword: raise BadRequestException('Passwords do not match.')
    u=User(id=uid('CUS'),fullName=req.fullName.strip(),email=req.email.lower(),phone=req.phone,dob=req.dob,emergencyContact=req.emergencyContact,password=req.password,country='India',role='customer',status='active',membership='none',loyaltyPoints=0,createdAt=now_iso()); dbs.add(u); dbs.commit(); audit(dbs,u.email,'Login','Account Registered','Self-registered'); notify(dbs,u.email,'Welcome to JetLease','Your account has been created. Explore the fleet and book your first flight.','success'); t=uuid.uuid4().hex; sessions[t]={'email':u.email,'role':u.role}; return {'token':t,'email':u.email,'fullName':u.fullName,'role':u.role}
@app.post('/api/auth/login')
def login(req:LoginRequest,dbs:Session=Depends(db)):
    ident=(req.identifier or '');
    u=dbs.execute(select(User).where(User.role=='customer', User.phone==ident if req.identifierType=='phone' else func.lower(User.email)==ident.lower(), User.password==req.password)).scalar_one_or_none()
    if u is None: raise UnauthorizedException('Invalid credentials. Try demo@jetlease.in / Demo@123')
    if u.status=='suspended': raise UnauthorizedException('This account has been suspended. Contact support.')
    audit(dbs,u.email,'Login','Customer Login',''); t=uuid.uuid4().hex; sessions[t]={'email':u.email,'role':u.role}; return {'token':t,'email':u.email,'fullName':u.fullName,'role':u.role}
@app.post('/api/auth/login/otp/request')
def otp(): return {'message':'OTP sent (demo code: 123456).'}
@app.post('/api/auth/admin-login')
def admin_login(req:AdminLoginRequest,dbs:Session=Depends(db)):
    u=dbs.execute(select(User).where(func.lower(User.email)==(req.email or '').lower(),User.password==req.password,User.role=='admin')).scalar_one_or_none()
    if u is None: raise UnauthorizedException('Invalid admin credentials.')
    audit(dbs,u.email,'Login','Admin Login',''); t=uuid.uuid4().hex; sessions[t]={'email':u.email,'role':u.role}; return {'token':t,'email':u.email,'fullName':u.fullName,'role':u.role}
@app.post('/api/auth/logout')
def logout(request:Request):
    h=request.headers.get('Authorization');
    if h and h.startswith('Bearer '): sessions.pop(h[7:].strip(),None)
    return {'message':'Logged out.'}
@app.get('/api/auth/session')
def session(request:Request,dbs:Session=Depends(db)):
    info=require_auth(request,dbs); h=request.headers.get('Authorization'); u=dbs.execute(select(User).where(User.email==info['email'])).scalar_one_or_none(); return {'token':h[7:].strip(),'email':u.email,'fullName':u.fullName,'role':u.role}
@app.post('/api/auth/forgot-password/request')
def fp_req(req:ForgotPasswordRequest,dbs:Session=Depends(db)):
    if dbs.execute(select(User).where(func.lower(User.email)==(req.email or '').lower())).scalar_one_or_none() is None: raise BadRequestException('No account found with that email.')
    return {'message':f'Mock OTP sent to {req.email} (use 123456 for this demo).'}
@app.post('/api/auth/forgot-password/confirm')
def fp_confirm(req:ResetPasswordRequest,dbs:Session=Depends(db)):
    if (req.otp or '').strip()!='123456': raise BadRequestException('Incorrect OTP. Use 123456 for this demo.')
    if req.newPassword is None or len(req.newPassword)<8: raise BadRequestException('Password must be at least 8 characters.')
    if req.newPassword!=req.confirmPassword: raise BadRequestException('Passwords do not match.')
    u=dbs.execute(select(User).where(func.lower(User.email)==(req.email or '').lower())).scalar_one_or_none()
    if u is None: raise NotFoundException('User not found.')
    u.password=req.newPassword; dbs.commit(); audit(dbs,u.email,'Login','Password Reset',''); return {'message':'Password updated successfully.'}

@app.get('/api/routes')
def routes(dbs:Session=Depends(db)): return obj(dbs.scalars(select(Route)).all())
@app.get('/api/routes/distance')
def dist(origin:str,destination:str,dbs:Session=Depends(db)): return {'distanceKm':distance_km(dbs,origin,destination)}
@app.get('/api/aircraft')
def aircraft(dbs:Session=Depends(db)): return obj(dbs.scalars(select(Aircraft)).all())
@app.get('/api/aircraft/available')
def available_aircraft(pax:int,category:Optional[str]=None,dbs:Session=Depends(db)):
    q=select(Aircraft).where(Aircraft.status=='Available',Aircraft.capacity>=pax); q=q.where(Aircraft.category==category) if category and category.strip() else q; return obj(dbs.scalars(q).all())
@app.post('/api/aircraft/recommend')
def recommend(req:RecommendRequest,dbs:Session=Depends(db)):
    q=select(Aircraft).where(Aircraft.status=='Available',Aircraft.capacity>=req.pax,Aircraft.rangeKm>=req.distanceKm); q=q.where(Aircraft.category==req.category) if req.category else q; results=[]
    for a in dbs.scalars(q).all(): results.append({'id':a.id,'model':a.model,'capacity':a.capacity,'range':a.rangeKm,'estCost':jround(a.hourlyRate*estimate_hours(req.distanceKm,a.speed,False)*1.2)})
    results.sort(key=lambda x:x['estCost']);
    if not results:return {'best':None,'alternatives':[]}
    best=next((r for r in results if r['estCost']<=req.budget),results[0]); others=[r for r in results if r['id']!=best['id']][:3]; return {'best':best,'alternatives':others}

@app.get('/api/guest/fleet')
def fleet(dbs:Session=Depends(db)): return obj(dbs.scalars(select(Aircraft)).all())
@app.get('/api/guest/faq')
def faq(dbs:Session=Depends(db)): return obj(dbs.scalars(select(Faq)).all())
@app.get('/api/guest/testimonials')
def testimonials(dbs:Session=Depends(db)): return obj(dbs.scalars(select(Testimonial)).all())
@app.post('/api/guest/contact')
def contact(req:ContactRequest,dbs:Session=Depends(db)):
    for m in [name(req.name),phone10(req.phone),email(req.email),message(req.message)]:
        if m: raise BadRequestException(m)
    x=ContactMessage(id=uid('MSG'),name=req.name,phone=req.phone,email=req.email,message=req.message,status='Unread',createdAt=now_iso());dbs.add(x);dbs.commit();return obj(x)

@app.post('/api/bookings/verify-aadhaar')
def verify_aadhaar(req:VerifyAadhaarRequest,request:Request,dbs:Session=Depends(db)):
    require_auth(request,dbs,'customer'); r=dbs.execute(select(AadhaarRegistry).where(AadhaarRegistry.aadhaarNumber==(req.aadhaar or '').strip())).scalar_one_or_none(); out={'verified':False,'holderName':None,'dob':None,'gender':None,'licenseClass':None,'hoursOnRecord':0,'message':''}
    if r is None: out['message']='Aadhaar number not found in the registry.'
    elif r.status!='Active': out['message']=f'Aadhaar found but its status is "{r.status}", not Active.'
    else: out.update(verified=True,holderName=r.holderName,dob=r.dob,gender=r.gender,message=f'Aadhaar verified - registered to {r.holderName}.')
    return out
@app.post('/api/bookings/verify-license')
def verify_license(req:VerifyLicenseRequest,request:Request,dbs:Session=Depends(db)):
    require_auth(request,dbs,'customer'); r=dbs.execute(select(PilotLicenseRegistry).where(func.lower(PilotLicenseRegistry.licenseNumber)==(req.licenseNumber or '').strip().lower())).scalar_one_or_none(); out={'verified':False,'holderName':None,'dob':None,'gender':None,'licenseClass':None,'hoursOnRecord':0,'message':''}
    if r is None: out['message']='License number not found in the DGCA registry.'
    elif r.status!='Active': out['message']=f'License found but its status is "{r.status}", not Active.'
    else: out.update(verified=True,holderName=r.holderName,licenseClass=r.licenseClass,hoursOnRecord=r.hoursOnRecord,message=f'License verified - registered to {r.holderName}, {r.hoursOnRecord} hours on record.')
    return out
@app.post('/api/bookings')
def create_booking(req:CreateBookingRequest,request:Request,dbs:Session=Depends(db)):
    user=require_auth(request,dbs,'customer');
    if req.type not in ('Domestic Charter','Helicopter Charter'): raise BadRequestException("Charter type must be 'Domestic Charter' or 'Helicopter Charter'.")
    if req.tripType not in ('One Way','Round Trip'): raise BadRequestException("Trip type must be 'One Way' or 'Round Trip'.")
    if dbs.get(Route,req.origin) is None or dbs.get(Route,req.destination) is None: raise BadRequestException('Unknown origin or destination airport code.')
    if req.date is None or req.date<today_iso(): raise BadRequestException('Departure date cannot be in the past.')
    if req.tripType=='Round Trip' and (req.returnDate is None or req.returnDate<req.date): raise BadRequestException('Return date cannot be before the departure date.')
    if req.pax<1 or req.pax>14: raise BadRequestException('Number of passengers must be between 1 and 14.')
    if req.passengers is None or len(req.passengers)!=req.pax: raise BadRequestException('Passenger details must be provided for every passenger.')
    a=dbs.get(Aircraft,req.aircraftId)
    if a is None: raise NotFoundException('Selected aircraft not found.')
    if a.status!='Available': raise BadRequestException('Selected aircraft is no longer available.')
    if a.capacity<req.pax: raise BadRequestException('Selected aircraft does not have enough seats.')
    if req.type=='Helicopter Charter' and a.category!='Helicopter': raise BadRequestException('Selected aircraft is not a helicopter.')
    if req.selfFly:
        if req.selfFlyDetails is None: raise BadRequestException('Self-fly details are required.')
        sf=req.selfFlyDetails; e=license_number(sf.licenseNumber)
        if e: raise BadRequestException(e)
        if not sf.verified and sf.flyingHours<100: raise BadRequestException('Self-fly requires at least 100 logged flying hours.')
        if sf.verified and sf.flyingHours<100: raise BadRequestException('DGCA record shows fewer than 100 flying hours - self-fly not permitted.')
        if not sf.dgcaDeclaration: raise BadRequestException('DGCA self-fly declaration must be accepted.')
    for p in req.passengers:
        e=name(p.name) or dob(p.dob)
        if e: raise BadRequestException(e)
    d=distance_km(dbs,req.origin,req.destination); c=cost_calc(a.hourlyRate,req.type,req.tripType,req.selfFly,d,a.speed)
    b=Booking(id=uid('BKG'),userEmail=user['email'],type=req.type,tripType=req.tripType,origin=req.origin,destination=req.destination,date_=req.date,time=req.time,returnDate=req.returnDate or '',returnTime=req.returnTime or '',pax=req.pax,aircraftId=a.id,aircraftModel=a.model,selfFly=req.selfFly,hours=c['hours'],aircraftCost=c['aircraftCost'],pilotCost=c['pilotCost'],crewCost=c['crewCost'],airportCharges=c['airportCharges'],fuelSurcharge=c['fuelSurcharge'],gst=c['gst'],total=c['total'],status='Pending Payment',createdAt=now_iso())
    if req.selfFly:
        sf=req.selfFlyDetails;b.licenseNumber=sf.licenseNumber;b.licenseClass=sf.licenseClass;b.flyingHours=sf.flyingHours;b.certificateFileName=sf.certificateFileName;b.dgcaDeclaration=sf.dgcaDeclaration;b.licenseVerified=sf.verified
    dbs.add(b)
    for pr in req.passengers: dbs.add(Passenger(bookingId=b.id,name=pr.name,dob=pr.dob,gender=pr.gender,aadhaar=pr.aadhaar,verificationStatus=pr.verificationStatus,noAadhaar=pr.noAadhaar,altDocumentId=pr.altDocumentId))
    a.status='Booked';dbs.commit();audit(dbs,user['email'],'Booking','Booking Created',b.id);notify(dbs,user['email'],'Booking Created',f'Booking {b.id} has been created. Complete payment to proceed.','success');notify(dbs,'admin','New Booking',f'New booking {b.id} created by {user["email"]}','info');return obj(b)
@app.get('/api/bookings/my')
def my_bookings(request:Request,dbs:Session=Depends(db)): u=require_auth(request,dbs,'customer'); return obj(dbs.scalars(select(Booking).where(Booking.userEmail==u['email']).order_by(Booking.createdAt.desc())).all())
@app.get('/api/bookings/{id}')
def get_booking(id:str,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer'); b=find_one(dbs,Booking,id,'Booking not found.')
    if b.userEmail!=u['email']: raise ForbiddenException('This booking does not belong to you.')
    return obj(b)
@app.get('/api/bookings/{id}/passengers')
def passengers(id:str,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer'); b=find_one(dbs,Booking,id,'Booking not found.')
    if b.userEmail!=u['email']: raise ForbiddenException('This booking does not belong to you.')
    return obj(dbs.scalars(select(Passenger).where(Passenger.bookingId==id)).all())
@app.post('/api/bookings/{id}/cancel')
def cancel_booking(id:str,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer'); b=find_one(dbs,Booking,id,'Booking not found.')
    if b.userEmail!=u['email']: raise ForbiddenException('This booking does not belong to you.')
    if b.status not in {'Pending Payment','Pending Verification','Lease Pending','Lease Signed','Approved'}: raise BadRequestException(f'Bookings with status "{b.status}" cannot be self-cancelled.')
    p=dbs.execute(select(Payment).where(Payment.bookingId==id)).scalar_one_or_none(); base=p.amount if p and p.status=='VERIFIED' else b.total; fee=jround(base*.20); refund=base-fee
    if p and p.status=='VERIFIED': p.cancellationFee=fee;p.refundAmount=refund;p.status='RETURNED'
    release_resources(dbs,b);void_unsigned_lease(dbs,id);b.status='Cancelled';b.assignedPilotId=None;b.assignedCrewIds=None;dbs.commit();audit(dbs,u['email'],'Booking','Booking Cancelled',f'{id} fee={fee} refund={refund}');notify(dbs,u['email'],'Booking Cancelled',f'Booking {id} was cancelled. Refund of INR {refund} will be processed.','info');notify(dbs,'admin','Booking Cancelled',f'Customer cancelled booking {id}','info');return {'message':'Booking cancelled.'}

@app.get('/api/payments/my')
def payments_my(request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer');return obj(dbs.scalars(select(Payment).where(Payment.userEmail==u['email']).order_by(Payment.submittedAt.desc())).all())
@app.get('/api/payments/booking/{bookingId}')
def payable(bookingId:str,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer'); b=find_one(dbs,Booking,bookingId,'Booking not found.')
    if b.userEmail!=u['email']: raise BadRequestException('This booking does not belong to you.')
    if b.status not in {'Pending Payment','Payment Rejected'}: raise BadRequestException(f'Booking status "{b.status}" is not payable.')
    return obj(b)
@app.post('/api/payments/booking/{bookingId}/pay')
def pay(bookingId:str,req:PayRequest,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer'); b=find_one(dbs,Booking,bookingId,'Booking not found.')
    if b.userEmail!=u['email']: raise BadRequestException('This booking does not belong to you.')
    if b.status not in {'Pending Payment','Payment Rejected'}: raise BadRequestException(f'Booking status "{b.status}" is not payable.')
    if not req.transactionId or not req.transactionId.strip(): raise BadRequestException('Transaction ID is required.')
    dbs.add(BankLedger(transactionId=req.transactionId,bookingId=bookingId,amount=b.total,status='CLEARED',clearedAt=now_iso()));p=Payment(id=uid('PAY'),bookingId=bookingId,userEmail=u['email'],amount=b.total,transactionId=req.transactionId,status='PENDING_VERIFICATION',submittedAt=now_iso(),cancellationFee=0,refundAmount=0);dbs.add(p);b.status='Pending Verification';dbs.commit();audit(dbs,u['email'],'Payment','Payment Submitted',f'{bookingId} - {req.transactionId}');notify(dbs,'admin','Payment Submitted',f'Payment submitted for {bookingId} by {u["email"]}','info');notify(dbs,u['email'],'Payment Submitted',f'We received your transaction ID for {bookingId}. Verification is in progress.','info');return obj(p)

@app.get('/api/leases/my')
def leases_my(request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer');return obj(dbs.scalars(select(Lease).where(Lease.userEmail==u['email']).order_by(Lease.createdAt.desc())).all())
def owned_lease(dbs,id,email_):
    l=find_one(dbs,Lease,id,'Lease not found.');
    if l.userEmail!=email_: raise ForbiddenException('This lease does not belong to you.')
    return l
@app.get('/api/leases/{id}')
def get_lease(id:str,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer');l=owned_lease(dbs,id,u['email']);b=find_one(dbs,Booking,l.bookingId,'Booking not found.');return {'lease':obj(l),'contractText':lease_text(l,b)}
@app.post('/api/leases/{id}/sign')
def sign_lease(id:str,req:SignLeaseRequest,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer');l=owned_lease(dbs,id,u['email'])
    if l.status!='Sent': raise BadRequestException('Only a lease with status "Sent" can be signed.')
    if not req.legalName or len(req.legalName.strip())<3: raise BadRequestException('Enter your full legal name.')
    l.status='Signed';l.signedBy=req.legalName.strip();l.signedDate=today_iso();b=find_one(dbs,Booking,l.bookingId,'Booking not found.');b.status='Lease Signed';dbs.commit();audit(dbs,u['email'],'Lease','Lease Signed',id);notify(dbs,'admin','Lease Signed',f'Lease {id} was signed by {u["email"]}. Awaiting approval.','info');notify(dbs,u['email'],'Lease Signed',f'You signed lease {id}. It is now awaiting admin approval.','success');return obj(l)
@app.get('/api/leases/{id}/export')
def export_lease(id:str,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer');l=owned_lease(dbs,id,u['email']);b=find_one(dbs,Booking,l.bookingId,'Booking not found.');return Response(content=lease_text(l,b),media_type='text/plain',headers={'Content-Disposition':f'attachment; filename="{id}.txt"'})

@app.get('/api/notifications/my')
def notifications_my(request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer');return obj(dbs.scalars(select(Notification).where(Notification.userEmail==u['email']).order_by(Notification.createdAt.desc())).all())
@app.post('/api/notifications/mark-read')
def notifications_read(request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer'); rows=dbs.scalars(select(Notification).where(Notification.userEmail==u['email'])).all();[setattr(n,'read',True) for n in rows];dbs.commit();return {'message':'All notifications marked as read.'}

@app.get('/api/profile')
def profile_get(request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer');return obj(dbs.execute(select(User).where(User.email==u['email'])).scalar_one())
@app.put('/api/profile')
def profile_update(req:UpdateProfileRequest,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer');e=name(req.fullName) or dob(req.dob)
    if e: raise BadRequestException(e)
    if not is_adult(req.dob): raise BadRequestException('You must be 18 or older.')
    e=phone10(req.emergencyContact)
    if e: raise BadRequestException(e)
    x=dbs.execute(select(User).where(User.email==u['email'])).scalar_one();x.fullName=req.fullName;x.dob=req.dob;x.emergencyContact=req.emergencyContact;dbs.commit();audit(dbs,u['email'],'Login','Profile Updated','');return obj(x)
@app.put('/api/profile/phone')
def phone_update(req:ChangePhoneRequest,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer');e=phone10(req.newPhone)
    if e: raise BadRequestException(e)
    if (req.otp or '').strip()!='123456': raise BadRequestException('Incorrect OTP. Phone number not changed. (Use 123456 for this demo.)')
    x=dbs.execute(select(User).where(User.email==u['email'])).scalar_one();x.phone=req.newPhone;dbs.commit();audit(dbs,u['email'],'Login','Phone Number Changed',req.newPhone);return obj(x)
@app.post('/api/reports')
def report(req:ReportIssueRequest,request:Request,dbs:Session=Depends(db)):
    u=require_auth(request,dbs,'customer');b=find_one(dbs,Booking,req.bookingId,'Booking not found.')
    if b.userEmail!=u['email']: raise BadRequestException('This booking does not belong to you.')
    if b.status not in {'Dispatched','Completed'}: raise BadRequestException('You can only report an issue for a Dispatched or Completed flight.')
    if not req.subject or not req.subject.strip(): raise BadRequestException('Subject is required.')
    e=message(req.details)
    if e: raise BadRequestException(e)
    r=Report(id=uid('RPT'),bookingId=req.bookingId,userEmail=u['email'],subject=req.subject,details=req.details,status='Open',createdAt=now_iso());dbs.add(r);dbs.commit();notify(dbs,'admin','New Issue Report',f'Report filed for booking {req.bookingId} by {u["email"]}','warning');return obj(r)

# ---------------- admin ----------------
def admin(request,dbs): return require_auth(request,dbs,'admin')
@app.get('/api/admin/overview')
def admin_overview(request:Request,dbs:Session=Depends(db)):
    admin(request,dbs); bs=dbs.scalars(select(Booking)).all(); stats={'totalBookings':len(bs),'bookingsByStatus':{},'revenueCompleted':sum(b.total for b in bs if b.status=='Completed'),'aircraftByStatus':{},'popularAircraft':{}}
    for b in bs: stats['bookingsByStatus'][b.status]=stats['bookingsByStatus'].get(b.status,0)+1
    for a in dbs.scalars(select(Aircraft)).all(): stats['aircraftByStatus'][a.status]=stats['aircraftByStatus'].get(a.status,0)+1
    pop={};
    for b in bs: pop[b.aircraftModel]=pop.get(b.aircraftModel,0)+1
    stats['popularAircraft']=dict(sorted(pop.items(),key=lambda kv:kv[1],reverse=True)[:3]);return stats
@app.get('/api/admin/audit-log')
def admin_audit(category:Optional[str]=None,request:Request=None,dbs:Session=Depends(db)):
    admin(request,dbs);q=select(AuditLog).order_by(AuditLog.timestamp.desc());q=q.where(AuditLog.category==category) if category and category.lower()!='all' else q;return obj(dbs.scalars(q).all())
@app.get('/api/admin/routes')
def admin_routes(request:Request,dbs:Session=Depends(db)):
    admin(request,dbs);out=[]
    for r in dbs.scalars(select(Route)).all(): out.append({'code':r.code,'city':r.city,'airport':r.airport,'bookingCount':len(dbs.scalars(select(Booking).where(or_(Booking.origin==r.code,Booking.destination==r.code))).all())})
    return out

def csv_response(filename,rows,headers):
    s=io.StringIO();w=csv.writer(s,lineterminator='\n');w.writerow(headers);w.writerows(rows);return Response(s.getvalue(),media_type='text/csv',headers={'Content-Disposition':f'attachment; filename="{filename}"'})
@app.get('/api/admin/exports/bookings.csv')
def export_b(request:Request,dbs:Session=Depends(db)):
    admin(request,dbs);return csv_response('bookings.csv',[[b.id,b.userEmail,b.type,b.tripType,b.origin,b.destination,b.date_,b.status,b.total] for b in dbs.scalars(select(Booking)).all()],['id','user_email','type','trip_type','origin','destination','date','status','total'])
@app.get('/api/admin/exports/customers.csv')
def export_c(request:Request,dbs:Session=Depends(db)):
    admin(request,dbs);return csv_response('customers.csv',[[u.id,u.fullName,u.email,u.phone,u.status,u.membership,u.loyaltyPoints] for u in dbs.scalars(select(User).where(User.role=='customer')).all()],['id','full_name','email','phone','status','membership','loyalty_points'])
@app.get('/api/admin/exports/payments.csv')
def export_p(request:Request,dbs:Session=Depends(db)):
    admin(request,dbs);return csv_response('payments.csv',[[p.id,p.bookingId,p.userEmail,p.amount,p.transactionId,p.status] for p in dbs.scalars(select(Payment)).all()],['id','booking_id','user_email','amount','transaction_id','status'])

@app.get('/api/admin/aircraft')
def admin_aircraft(request:Request,dbs:Session=Depends(db)): admin(request,dbs);return obj(dbs.scalars(select(Aircraft)).all())
@app.post('/api/admin/aircraft')
def admin_add_aircraft(req:AddAircraftRequest,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);a=Aircraft(id=uid('AC'),reg=req.reg,model=req.model,manufacturer=req.manufacturer,category=req.category,capacity=req.capacity,speed=req.speed,rangeKm=req.rangeKm,hourlyRate=req.hourlyRate,status='Available',typeRating=req.typeRating);dbs.add(a);dbs.commit();audit(dbs,u['email'],'Admin','Aircraft Added',f'{a.id} - {req.model}');return obj(a)
@app.put('/api/admin/aircraft/{id}/status')
def admin_aircraft_status(id:str,req:StatusRequest,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);a=find_one(dbs,Aircraft,id,'Aircraft not found.');a.status=req.status;dbs.commit();audit(dbs,u['email'],'Admin','Aircraft Status Changed',f'{id} -> {req.status}');return obj(a)
@app.put('/api/admin/aircraft/{id}/rate')
def admin_aircraft_rate(id:str,req:RateRequest,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);a=find_one(dbs,Aircraft,id,'Aircraft not found.');a.hourlyRate=req.hourlyRate;dbs.commit();audit(dbs,u['email'],'Admin','Aircraft Rate Changed',f'{id} -> {req.hourlyRate}');return obj(a)
@app.delete('/api/admin/aircraft/{id}')
def admin_aircraft_delete(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);a=find_one(dbs,Aircraft,id,'Aircraft not found.');
    if a.status=='Booked': raise BadRequestException('Cannot delete an aircraft that is currently booked.')
    dbs.delete(a);dbs.commit();audit(dbs,u['email'],'Admin','Aircraft Deleted',f'{id} - {a.model}');return {'message':'Aircraft deleted.'}

@app.get('/api/admin/bookings')
def admin_bookings(request:Request,dbs:Session=Depends(db)): admin(request,dbs);return obj(dbs.scalars(select(Booking).order_by(Booking.createdAt.desc())).all())
@app.get('/api/admin/bookings/{id}')
def admin_booking(id:str,request:Request,dbs:Session=Depends(db)): admin(request,dbs);return obj(find_one(dbs,Booking,id,'Booking not found.'))
@app.post('/api/admin/bookings/{id}/assign-crew')
def admin_assign(id:str,req:AssignCrewRequest,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);b=find_one(dbs,Booking,id,'Booking not found.');p=find_one(dbs,Pilot,req.pilotId,'Pilot not found.')
    if not p.available or p.remainingHours<b.hours: raise BadRequestException('This pilot does not have enough remaining hours for this flight.')
    if not req.crewIds: raise BadRequestException('At least one crew member must be selected.')
    cs=[find_one(dbs,Crew,cid,f'Crew member not found: {cid}') for cid in req.crewIds]
    for c in cs:
        if not c.available or c.remainingHours<b.hours: raise BadRequestException(f'Crew member {c.id} does not have enough remaining hours.')
    p.remainingHours-=b.hours
    for c in cs:c.remainingHours-=b.hours
    b.assignedPilotId=p.id;b.assignedCrewIds=','.join(req.crewIds);dbs.commit();audit(dbs,u['email'],'Admin','Crew Assigned',f'{id} pilot={p.id} crew={b.assignedCrewIds}');return obj(b)
def advance_admin(id,new_status,u,dbs):
    b=find_one(dbs,Booking,id,'Booking not found.');b.status=new_status;dbs.commit();audit(dbs,u['email'],'Admin','Booking Advanced',f'{id} -> {new_status}');notify(dbs,b.userEmail,'Booking Update',f'Your booking {id} is now "{new_status}".','info');return b
@app.post('/api/admin/bookings/{id}/approve')
def admin_approve(id:str,request:Request,dbs:Session=Depends(db)): return obj(advance_admin(id,'Approved',admin(request,dbs),dbs))
@app.post('/api/admin/bookings/{id}/dispatch')
def admin_dispatch(id:str,request:Request,dbs:Session=Depends(db)): return obj(advance_admin(id,'Dispatched',admin(request,dbs),dbs))
@app.post('/api/admin/bookings/{id}/complete')
def admin_complete(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);b=advance_admin(id,'Completed',u,dbs);pts=jround(b.total/10000.0);cu=dbs.execute(select(User).where(User.email==b.userEmail)).scalar_one_or_none();
    if cu:cu.loyaltyPoints+=pts;dbs.commit()
    notify(dbs,b.userEmail,'Loyalty Points Earned',f'You earned {pts} loyalty points for booking {id}.','success');return obj(b)
@app.post('/api/admin/bookings/{id}/reject')
def admin_reject(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);b=find_one(dbs,Booking,id,'Booking not found.');p=dbs.execute(select(Payment).where(Payment.bookingId==id,Payment.status=='VERIFIED')).scalar_one_or_none()
    if p:p.refundAmount=p.amount;p.cancellationFee=0;p.status='RETURNED'
    release_resources(dbs,b);void_unsigned_lease(dbs,id);b.status='Rejected';b.assignedPilotId=None;b.assignedCrewIds=None;dbs.commit();audit(dbs,u['email'],'Admin','Booking Rejected',id);notify(dbs,b.userEmail,'Booking Rejected',f'Booking {id} was rejected by our team.','warning');return obj(b)

@app.get('/api/admin/payments')
def admin_payments(request:Request,dbs:Session=Depends(db)):admin(request,dbs);return obj(dbs.scalars(select(Payment).order_by(Payment.submittedAt.desc())).all())
@app.get('/api/admin/payments/{id}/ledger-check')
def ledger(id:str,request:Request,dbs:Session=Depends(db)):
    admin(request,dbs);p=find_one(dbs,Payment,id,'Payment not found.');e=dbs.execute(select(BankLedger).where(BankLedger.transactionId==p.transactionId,BankLedger.bookingId==p.bookingId)).scalar_one_or_none()
    if e is None:return {'verified':False,'message':'Transaction ID not found in the bank ledger.'}
    if e.amount!=p.amount:return {'verified':False,'message':'Transaction found, but the settled amount does not match the invoice.'}
    return {'verified':True,'message':f'Bank ledger confirms this transaction cleared for INR {e.amount} on {e.clearedAt}.'}
@app.post('/api/admin/payments/{id}/verify')
def verify_pay(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);p=find_one(dbs,Payment,id,'Payment not found.')
    if p.status!='PENDING_VERIFICATION':raise BadRequestException(f'This payment is already "{p.status}" - nothing to verify.')
    p.status='VERIFIED';b=find_one(dbs,Booking,p.bookingId,'Booking not found.');b.status='Lease Pending';dbs.commit();ensure_lease(dbs,p.bookingId,p.userEmail);audit(dbs,u['email'],'Admin','Payment Verified',id);notify(dbs,p.userEmail,'Payment Verified',f'Your payment for {p.bookingId} has been verified. Your lease is ready.','success');return obj(p)
@app.post('/api/admin/payments/{id}/reject')
def reject_pay(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);p=find_one(dbs,Payment,id,'Payment not found.')
    if p.status!='PENDING_VERIFICATION':raise BadRequestException(f'This payment is already "{p.status}" - nothing to reject.')
    p.status='REJECTED';b=find_one(dbs,Booking,p.bookingId,'Booking not found.');b.status='Payment Rejected';dbs.commit();audit(dbs,u['email'],'Admin','Payment Rejected',id);notify(dbs,p.userEmail,'Payment Rejected',f'Your payment for {p.bookingId} was rejected. Please resubmit.','warning');return obj(p)

@app.get('/api/admin/leases')
def admin_leases(request:Request,dbs:Session=Depends(db)):admin(request,dbs);return obj(dbs.scalars(select(Lease).order_by(Lease.createdAt.desc())).all())
@app.post('/api/admin/leases/{id}/approve')
def approve_lease(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);l=find_one(dbs,Lease,id,'Lease not found.');
    if l.status!='Signed':raise BadRequestException(f'Only a lease with status "Signed" can be approved here. Current status: {l.status}')
    l.status='Approved';l.approvalDate=today_iso();b=find_one(dbs,Booking,l.bookingId,'Booking not found.');b.status='Approved';dbs.commit();audit(dbs,u['email'],'Admin','Lease Approved',id);notify(dbs,l.userEmail,'Lease Approved',f'Your lease for booking {l.bookingId} has been approved.','success');return obj(l)
@app.post('/api/admin/leases/{id}/reject')
def reject_lease(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);l=find_one(dbs,Lease,id,'Lease not found.')
    if l.status!='Signed':raise BadRequestException(f'Only a lease with status "Signed" can be rejected here. Current status: {l.status}')
    l.status='Rejected';p=dbs.execute(select(Payment).where(Payment.bookingId==l.bookingId,Payment.status=='VERIFIED')).scalar_one_or_none()
    if p:p.refundAmount=p.amount;p.cancellationFee=0;p.status='RETURNED'
    b=find_one(dbs,Booking,l.bookingId,'Booking not found.');release_resources(dbs,b);b.status='Rejected';b.assignedPilotId=None;b.assignedCrewIds=None;dbs.commit();audit(dbs,u['email'],'Admin','Lease Rejected',id);notify(dbs,l.userEmail,'Lease Rejected',f'Your lease for booking {l.bookingId} was rejected and your payment is being fully refunded.','warning');return obj(l)

@app.get('/api/admin/customers')
def customers(request:Request,dbs:Session=Depends(db)):admin(request,dbs);return obj(dbs.scalars(select(User).where(User.role=='customer')).all())
@app.get('/api/admin/customers/{email}/bookings')
def customer_bookings(email:str,request:Request,dbs:Session=Depends(db)):admin(request,dbs);return obj(dbs.scalars(select(Booking).where(Booking.userEmail==email).order_by(Booking.createdAt.desc())).all())
@app.post('/api/admin/customers/{email}/toggle-status')
def customer_toggle(email:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);x=dbs.execute(select(User).where(User.email==email)).scalar_one_or_none();
    if x is None:raise NotFoundException('Customer not found.')
    x.status='active' if x.status=='suspended' else 'suspended';dbs.commit();action='Suspended' if x.status=='suspended' else 'Reactivated';audit(dbs,u['email'],'Admin',f'Customer {action}',email);notify(dbs,email,f'Account {action}',f'Your account has been {x.status} by the JetLease team.','warning');return {'email':x.email,'status':x.status}
@app.get('/api/admin/pilots')
def pilots(request:Request,dbs:Session=Depends(db)):admin(request,dbs);return obj(dbs.scalars(select(Pilot)).all())
@app.get('/api/admin/crew')
def crew(request:Request,dbs:Session=Depends(db)):admin(request,dbs);return obj(dbs.scalars(select(Crew)).all())
@app.post('/api/admin/pilots/{id}/toggle-availability')
def pilot_toggle(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);x=find_one(dbs,Pilot,id,'Pilot not found.');x.available=not x.available;dbs.commit();audit(dbs,u['email'],'Admin','Pilot Availability Toggled',id);return obj(x)
@app.post('/api/admin/crew/{id}/toggle-availability')
def crew_toggle(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);x=find_one(dbs,Crew,id,'Crew member not found.');x.available=not x.available;dbs.commit();audit(dbs,u['email'],'Admin','Crew Availability Toggled',id);return obj(x)
@app.get('/api/admin/inbox/messages')
def messages(request:Request,dbs:Session=Depends(db)):admin(request,dbs);return obj(dbs.scalars(select(ContactMessage).order_by(ContactMessage.createdAt.desc())).all())
@app.post('/api/admin/inbox/messages/{id}/mark-read')
def mark_msg(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);x=find_one(dbs,ContactMessage,id,'Message not found.');x.status='Read';dbs.commit();audit(dbs,u['email'],'Admin','Contact Message Read',id);return obj(x)
@app.get('/api/admin/inbox/reports')
def admin_reports(request:Request,dbs:Session=Depends(db)):admin(request,dbs);return obj(dbs.scalars(select(Report).order_by(Report.createdAt.desc())).all())
@app.post('/api/admin/inbox/reports/{id}/resolve')
def resolve_report(id:str,request:Request,dbs:Session=Depends(db)):
    u=admin(request,dbs);r=find_one(dbs,Report,id,'Report not found.');r.status='Resolved';dbs.commit();audit(dbs,u['email'],'Admin','Issue Report Resolved',id);notify(dbs,r.userEmail,'Issue Resolved',f'Your reported issue for booking {r.bookingId} has been resolved.','success');return obj(r)

# ---------------- seed ----------------
def seed(dbs):
    if not SEED_ENABLED or dbs.query(Aircraft).count()>0:return
    routes=[('DEL','New Delhi','Indira Gandhi Intl',28.5562,77.1000),('BOM','Mumbai','Chhatrapati Shivaji Intl',19.0896,72.8656),('BLR','Bengaluru','Kempegowda Intl',13.1986,77.7066),('HYD','Hyderabad','Rajiv Gandhi Intl',17.2403,78.4294),('MAA','Chennai','Chennai Intl',12.9941,80.1709),('CCU','Kolkata','Netaji Subhas Chandra Bose Intl',22.6547,88.4467),('GOI','Goa','Dabolim',15.3808,73.8314),('JAI','Jaipur','Jaipur Intl',26.8242,75.8122),('COK','Kochi','Cochin Intl',10.1520,76.4019),('PNQ','Pune','Pune Airport',18.5822,73.9197)]
    for r in routes:dbs.add(Route(code=r[0],city=r[1],airport=r[2],lat=r[3],lon=r[4]))
    planes=[('Cessna Citation CJ3+','Textron','Light Jet',6,750,3700,220000,'N-CJ3'),('Embraer Legacy 500','Embraer','Mid Jet',9,850,5900,340000,'N-EMB500'),('Bombardier Global 6000','Bombardier','Heavy Jet',13,900,11000,680000,'N-GLB6000'),('Airbus H145','Airbus Helicopters','Helicopter',8,245,680,145000,'N-H145'),('Bell 429','Bell','Helicopter',6,260,722,130000,'N-B429'),('Beechcraft King Air 350i','Beechcraft','Turboprop',9,578,3300,165000,'N-KA350'),('Pilatus PC-24','Pilatus','Light Jet',8,815,3610,260000,'N-PC24'),('Gulfstream G650','Gulfstream','Heavy Jet',14,956,13890,850000,'N-G650')]
    for model,mfr,cat,cap,spd,rng,rate,tr in planes:dbs.add(Aircraft(id=uid('AC'),reg='VT-'+re.sub('[^A-Za-z0-9]','',tr).upper(),model=model,manufacturer=mfr,category=cat,capacity=cap,speed=spd,rangeKm=rng,hourlyRate=rate,status='Available',typeRating=tr))
    for n,l,h in [('Capt. Arjun Mehta','DGCA-ATPL-10234',900),('Capt. Neha Kapoor','DGCA-ATPL-10567',750),('Capt. Rohan Verma','DGCA-ATPL-10890',1000),('Capt. Sara Iyer','DGCA-ATPL-11123',620)]:dbs.add(Pilot(id=uid('PLT'),name=n,licenseNumber=l,remainingHours=h,available=True))
    for n,r,h in [('Priya Nair','Flight Attendant',900),('Karan Malhotra','Flight Attendant',850),('Divya Menon','Flight Engineer',700),('Aditya Rao','Flight Attendant',950)]:dbs.add(Crew(id=uid('CRW'),name=n,role=r,remainingHours=h,available=True))
    dbs.add(User(id=uid('ADM'),fullName='JetLease Admin',email='admin@jetlease.in',phone='9000000000',dob='1985-01-01',emergencyContact='9000000001',password='Admin@123',country='India',role='admin',status='active',membership='none',loyaltyPoints=0,createdAt=now_iso()))
    dbs.add(User(id=uid('CUS'),fullName='Demo Customer',email='demo@jetlease.in',phone='9123456780',dob='1990-05-15',emergencyContact='9123456781',password='Demo@123',country='India',role='customer',status='active',membership='gold',loyaltyPoints=120,createdAt=now_iso()))
    for n,h,c,hrs,st in [('DGCA-ATPL-10234','Arjun Mehta','ATPL',900,'Active'),('DGCA-ATPL-10567','Neha Kapoor','ATPL',750,'Active'),('DGCA-CPL-99871','Demo Customer','CPL',145,'Active'),('DGCA-CPL-55021','Rahul Singh','CPL',60,'Active')]:dbs.add(PilotLicenseRegistry(licenseNumber=n,holderName=h,licenseClass=c,hoursOnRecord=hrs,status=st))
    for n,h,d,g in [('123456789012','Demo Customer','1990-05-15','Other'),('234567890123','Priya Sharma','1988-11-02','Female'),('345678901234','Ankit Gupta','1979-03-21','Male')]:dbs.add(AadhaarRegistry(aadhaarNumber=n,holderName=h,dob=d,gender=g,status='Active'))
    faqs=[('How far in advance should I book a charter?','We recommend booking at least 48 hours in advance, though urgent charters can often be accommodated with shorter notice depending on aircraft availability.'),('Can I bring pets on board?','Yes, most of our aircraft are pet-friendly. Please mention this during booking so we can prepare accordingly.'),('What documents do passengers need?','A valid government photo ID and Aadhaar (or an alternate ID document) are required for all passengers.'),('Can I self-fly the aircraft?','Yes, if you hold a valid DGCA license with at least 100 logged flying hours on the relevant type rating, you may opt for a self-fly charter during booking.'),('What is your cancellation policy?','Cancellations incur a 20% fee on the amount paid; the remaining 80% is refunded.')]
    for q,a in faqs:dbs.add(Faq(question=q,answer=a))
    dbs.add(Testimonial(name='Ritika Sharma',role='Business Executive',quote='JetLease made our Mumbai-Delhi commute effortless. Professional crew and immaculate aircraft.',rating=5));dbs.add(Testimonial(name='Vikram Oberoi',role='Film Producer',quote='Booked a last-minute helicopter charter for a shoot - seamless from booking to landing.',rating=5));dbs.add(Testimonial(name='Ananya Rao',role='Entrepreneur',quote='The self-fly option was a great touch. Loved having full control on my own terms.',rating=4));dbs.commit()

Base.metadata.create_all(engine)
with SessionLocal() as _s: seed(_s)

if __name__=='__main__':
    import uvicorn; uvicorn.run(app,host='0.0.0.0',port=PORT)
