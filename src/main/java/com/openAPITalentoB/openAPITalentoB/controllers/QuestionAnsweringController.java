package com.openAPITalentoB.openAPITalentoB.controllers;

import com.openAPITalentoB.openAPITalentoB.models.Document;
import com.openAPITalentoB.openAPITalentoB.models.QuestionRequest;
import com.openAPITalentoB.openAPITalentoB.services.DocumentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "https://jeanc4rl02.github.io/TalentoB-Front/")
public class QuestionAnsweringController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/ask")
    public String askQuestion(@ModelAttribute QuestionRequest request) {
        List<MultipartFile> files = request.getFiles();
        List<String> urls = request.getUrls();

        if ((files == null || files.isEmpty()) && (urls == null || urls.isEmpty())) {
            return "No se proporcionaron documentos para la consulta.";
        }

        StringBuilder contextBuilder = new StringBuilder();

        if (files != null) {
            for (MultipartFile file : files) {
                try {
                    contextBuilder.append(documentService.extractTextContent(file)).append(" ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (urls != null) {
            for (String url : urls) {
                try {
                    contextBuilder.append(documentService.fetchContentFromUrl(url)).append(" ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String context = contextBuilder.toString().trim();

        if (context.isEmpty()) {
            return "No se encontró información relacionada en los documentos proporcionados.";
        }

        return generateAnswer(request.getQuestion(), context);
    }

    private String generateAnswer(String question, String context) {
        String apiKey = System.getenv("API_KEY");

        // Construir el cuerpo de la solicitud JSON
        JSONObject body = new JSONObject();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", new JSONArray().put(new JSONObject().put("role", "system").put("content", "Eres un asistente útil."))
                .put(new JSONObject().put("role", "user").put("content", "Pregunta: " + question + "\n\nContexto: " + context + "\n\nRespuesta:")));
        body.put("max_tokens", 150);

        HttpResponse<JsonNode> response = Unirest.post("https://api.openai.com/v1/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .body(body)
                .asJson();

        if (!response.getBody().getObject().has("choices")) {
            return "Error al generar la respuesta. Por favor, inténtelo de nuevo.";
        }

        return response.getBody().getObject().getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim();
    }

    @PostMapping("/upload")
    public List<Document> uploadDocuments(@RequestParam(value = "files", required = false) MultipartFile[] files,
                                          @RequestParam("title") String title,
                                          @RequestParam("urls") String urlsJson) {
        try {
            int fileCount = (files != null) ? files.length : 0;
            int urlCount = getUrlCount(urlsJson);

            // Validar cantidad de archivos y URLs
            if (fileCount + urlCount < 3 || fileCount + urlCount > 5) {
                throw new RuntimeException("Debe proporcionar entre 3 y 5 archivos o URLs.");
            }

            // Procesar archivos
            if (files != null) {
                for (MultipartFile file : files) {
                    documentService.saveDocument(file, title);
                }
            }

            // Procesar URLs
            List<String> urls = new ObjectMapper().readValue(urlsJson, new TypeReference<List<String>>() {});
            for (String url : urls) {
                documentService.saveDocumentFromUrl(url, title);
            }

            return documentService.getAllDocuments();
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar los archivos", e);
        }
    }


    private int getUrlCount(String urlsJson) {
        try {
            List<String> urls = objectMapper.readValue(urlsJson, new TypeReference<List<String>>() {});
            return urls.size();
        } catch (Exception e) {
            return 0;
        }
    }
}
