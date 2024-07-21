package com.example.app_rag.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RagController {

    private ChatClient chatClient;

    private VectorStore vectorStore;

    @Value("classpath:/prompts/prompt.st")
    private Resource resource;

    public RagController(
            ChatClient.Builder chatClient,
            VectorStore vectorStore) {
        this.chatClient = chatClient.build();
        this.vectorStore = vectorStore;
    }

    @GetMapping("/chat")
    public String ask(String question) {
        return chatClient.prompt().user(question).call().content();
    }

    @PostMapping("/ask")
    public String askWithContext(String question) {

        PromptTemplate promptTemplate = new PromptTemplate(resource);

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.query(question).withTopK(2));

        List<String> context = documents.stream().map(d -> d.getContent()).toList();

        Prompt prompt = promptTemplate.create(Map.of("context", context, "question", question));

        String content = chatClient.prompt(prompt).call().content();
        return content;
    }

}
