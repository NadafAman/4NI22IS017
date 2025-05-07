package com.example.demo.controller;

import com.example.demo.model.NumberResponse;
import com.example.demo.service.NumberService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/numbers")
public class NumberController {

    private final NumberService numberService;

    public NumberController(NumberService numberService) {
        this.numberService = numberService;
    }

    @GetMapping("/{numberid}")
    public NumberResponse getAverage(@PathVariable String numberid) {
        return numberService.fetchAndCalculate(numberid);
    }
}
