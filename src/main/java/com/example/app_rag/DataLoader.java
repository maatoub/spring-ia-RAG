package com.example.app_rag;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    @Value("classpath:/pdfs/CV.pdf")
    private Resource pdfFile;

    @Value("vs1.json")
    private String vectorStoreName;

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = new SimpleVectorStore(embeddingModel);
        String path = Path.of("src", "main", "resources", "vectorStore").toFile().getAbsolutePath() + "/"
                + vectorStoreName;
        File fileStore = new File(path);
        if (fileStore.exists()) {
            System.out.println("Fichier est exist : " + path);
            simpleVectorStore.load(fileStore);
        } else {
            PagePdfDocumentReader documentReader = new PagePdfDocumentReader(pdfFile);
            List<Document> documents = documentReader.get();
            TextSplitter textSplitter = new TokenTextSplitter();
            List<Document> chunks = textSplitter.split(documents);
            try {
                simpleVectorStore.add(chunks);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout de chunks au VectorStore : " + e.getMessage());
                e.printStackTrace();
            }
            simpleVectorStore.save(fileStore);
        }
        return simpleVectorStore;
    }
}
