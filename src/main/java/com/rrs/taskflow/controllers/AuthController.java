package com.rrs.taskflow.controllers;

import com.rrs.taskflow.dtos.RegisterRequestDto;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @PostMapping("/register-user")
    public RequestEntity<> registerUser(@RequestBody RegisterRequestDto @RequestParam ){

    }
}
