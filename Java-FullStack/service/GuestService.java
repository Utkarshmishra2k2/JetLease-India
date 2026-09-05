package com.jetlease.service;

import com.jetlease.dto.request.ContactRequest;
import com.jetlease.entity.Aircraft;
import com.jetlease.entity.ContactMessage;
import com.jetlease.entity.Faq;
import com.jetlease.entity.Testimonial;
import com.jetlease.exception.BadRequestException;
import com.jetlease.repository.AircraftRepository;
import com.jetlease.repository.ContactMessageRepository;
import com.jetlease.repository.FaqRepository;
import com.jetlease.repository.TestimonialRepository;
import com.jetlease.util.IdGen;
import com.jetlease.util.Validators;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuestService {

    private final AircraftRepository aircraftRepository;
    private final FaqRepository faqRepository;
    private final TestimonialRepository testimonialRepository;
    private final ContactMessageRepository contactMessageRepository;

    public GuestService(AircraftRepository aircraftRepository, FaqRepository faqRepository,
                         TestimonialRepository testimonialRepository, ContactMessageRepository contactMessageRepository) {
        this.aircraftRepository = aircraftRepository;
        this.faqRepository = faqRepository;
        this.testimonialRepository = testimonialRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    public List<Aircraft> fleet() {
        return aircraftRepository.findAll();
    }

    public List<Faq> faq() {
        return faqRepository.findAll();
    }

    public List<Testimonial> testimonials() {
        return testimonialRepository.findAll();
    }

    public ContactMessage submitContact(ContactRequest req) {
        String nameErr = Validators.name(req.getName());
        if (!nameErr.isEmpty()) throw new BadRequestException(nameErr);
        String phoneErr = Validators.phone10(req.getPhone());
        if (!phoneErr.isEmpty()) throw new BadRequestException(phoneErr);
        String emailErr = Validators.email(req.getEmail());
        if (!emailErr.isEmpty()) throw new BadRequestException(emailErr);
        String msgErr = Validators.message(req.getMessage());
        if (!msgErr.isEmpty()) throw new BadRequestException(msgErr);

        ContactMessage msg = new ContactMessage();
        msg.setId(IdGen.uid("MSG"));
        msg.setName(req.getName());
        msg.setPhone(req.getPhone());
        msg.setEmail(req.getEmail());
        msg.setMessage(req.getMessage());
        msg.setStatus("Unread");
        msg.setCreatedAt(IdGen.nowIso());
        return contactMessageRepository.save(msg);
    }
}
