package com.openAPITalentoB.openAPITalentoB.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class QuestionRequest {
    private String question;
    private List<MultipartFile> files;
    private List<String> urls;

}
