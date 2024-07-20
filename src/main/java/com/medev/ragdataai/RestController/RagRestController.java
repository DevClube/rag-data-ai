package com.medev.ragdataai.RestController;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RagRestController {

    private ChatClient chatClient;

    @Value("classpath:prompt/promptMessage.st")
    private Resource promptResource;

    private SimpleVectorStore vectorStore;

    public RagRestController(ChatClient.Builder chatClient, SimpleVectorStore vectorStore) {
        this.chatClient = chatClient.build();
        this.vectorStore = vectorStore;
    }

    @PostMapping("/chat")
    public String chat(String question) {

        PromptTemplate promptTemplate = new PromptTemplate(promptResource);

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.query(question)
        );

        List<String> context =  documents.stream().map(d -> d.getContent()).toList();

        Prompt prompt = promptTemplate.create(Map.of("context", context, "question", question));

        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
