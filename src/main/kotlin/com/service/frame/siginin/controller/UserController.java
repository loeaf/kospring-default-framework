package com.service.frame.siginin.controller;

import com.service.frame.rstmeet.dto.ResResult;
import com.service.frame.siginin.dto.param.UserParam;
import com.service.frame.siginin.service.SigininService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private final SigininService sigininService;

    @GetMapping
    public String getSignUpForm() {
        return "signUp";
    }

    @PostMapping("/signUp")
    public ResponseEntity<ResResult> signUp(HttpServletRequest request,
                                            @RequestBody UserParam userForm) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        var result = sigininService.signUp(userForm);
        return ResponseEntity.ok(new ResResult(result));
    }
}
