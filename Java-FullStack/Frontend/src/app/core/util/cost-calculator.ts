import { CostBreakdown } from '../models/models';

/**
 * Client-side mirror of CostCalculatorService.java, used only to render the Step 4
 * cost preview before the booking is actually submitted. The authoritative cost is
 * always the one returned by the real POST /api/bookings response — this exists
 * purely so the customer sees the breakdown before committing, matching the
 * reference app's preview-then-confirm flow.
 */
const PILOT_RATE_PER_HOUR = 45_000;
const CREW_RATE_PER_HOUR = 12_000;
const CREW_COUNT = 2;
const DOMESTIC_AIRPORT_CHARGES = 35_000;
const HELICOPTER_AIRPORT_CHARGES = 18_000;
const FUEL_SURCHARGE_RATE = 0.08;
const GST_RATE = 0.05;

export function estimateHours(distanceKm: number, speedKmh: number, roundTrip: boolean): number {
  const oneWayKm = roundTrip ? distanceKm * 2 : distanceKm;
  let hours = oneWayKm / speedKmh;
  hours = Math.ceil(hours * 4) / 4;
  return Math.max(hours, 1);
}

export function calculateCost(
  hourlyRate: number,
  bookingType: string,
  tripType: string,
  selfFly: boolean,
  distanceKm: number,
  speedKmh: number
): CostBreakdown {
  const roundTrip = tripType === 'Round Trip';
  const hours = distanceKm > 0 ? estimateHours(distanceKm, speedKmh, roundTrip) : roundTrip ? 4 : 2;

  const aircraftCost = Math.round(hourlyRate * hours);
  const pilotCost = selfFly ? Math.round((PILOT_RATE_PER_HOUR * hours) / 2) : Math.round(PILOT_RATE_PER_HOUR * hours);
  const crewCost = Math.round(CREW_RATE_PER_HOUR * CREW_COUNT * hours);
  const airportCharges = bookingType === 'Helicopter Charter' ? HELICOPTER_AIRPORT_CHARGES : DOMESTIC_AIRPORT_CHARGES;

  const subtotal = aircraftCost + pilotCost + crewCost + airportCharges;
  const fuelSurcharge = Math.round(subtotal * FUEL_SURCHARGE_RATE);
  const gst = Math.round((subtotal + fuelSurcharge) * GST_RATE);
  const total = subtotal + fuelSurcharge + gst;

  return { hours, aircraftCost, pilotCost, crewCost, airportCharges, fuelSurcharge, gst, total };
}