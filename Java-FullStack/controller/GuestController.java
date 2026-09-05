package com.jetlease.controller;

import com.jetlease.dto.request.ContactRequest;
import com.jetlease.entity.Aircraft;
import com.jetlease.entity.ContactMessage;
import com.jetlease.entity.Faq;
import com.jetlease.entity.Testimonial;
import com.jetlease.service.GuestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @GetMapping("/fleet")
    public List<Aircraft> fleet() {
        return guestService.fleet();
    }

    @GetMapping("/faq")
    public List<Faq> faq() {
        return guestService.faq();
    }

    @GetMapping("/testimonials")
    public List<Testimonial> testimonials() {
        return guestService.testimonials();
    }

    @PostMapping("/contact")
    public ContactMessage contact(@RequestBody ContactRequest req) {
        return guestService.submitContact(req);
    }
}
