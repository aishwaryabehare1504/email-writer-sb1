package com.email.writer;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
public class EmailGeneratorController {
    private final EmailGeneratorService emailGeneratorService;
    @PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest){
        String response=emailGeneratorService.generateEmailReply(emailRequest);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/demo")
    public ResponseEntity<String> demo() {
        EmailRequest request = new EmailRequest();
        request.setEmailContent("Hi, it was great meeting you at Google IO.");

        String email = emailGeneratorService.generateEmailReply(request);

        String formatted = email.replace("\n", "<br>");

        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(formatted);
    }
}
