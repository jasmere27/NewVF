package com.ai.agent.verifact.service;

import com.ai.agent.verifact.tool.VoiceToTextTool;
import com.ai.agent.verifact.tool.DateTimeTool;
import com.ai.agent.verifact.tool.GoogleSearchTool;
import com.ai.agent.verifact.tool.UriContentTool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final GoogleSearchTool googleSearchTool;
    private final DateTimeTool dateTimeTool;
    private final UriContentTool uriContentTool;
    private final VoiceToTextTool voiceToTextTool;

    @Autowired
    public AiService(ChatClient.Builder chatClientBuilder,
                     GoogleSearchTool googleSearchTool,
                     DateTimeTool dateTimeTool,
                     UriContentTool uriContentTool,
                     VoiceToTextTool voiceToTextTool) {
        this.chatClient = chatClientBuilder.build();
        this.googleSearchTool = googleSearchTool;
        this.dateTimeTool = dateTimeTool;
        this.uriContentTool = uriContentTool;
        this.voiceToTextTool = voiceToTextTool;
    }

    public String isFakeNews(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("News cannot be null or empty");
        }

        String contentToAnalyze = input;

        if (uriContentTool.isUrl(input)) {
            Optional<String> content = uriContentTool.fetchContentFromUrl(input);
            if (content.isPresent() && !content.get().isBlank()) {
                contentToAnalyze = content.get();
            } else {
                return "❗ Unable to fetch content from the URL provided. Please make sure it is accessible and contains readable text.";
            }
        }

        
        
        final PromptTemplate promptTemplate = new PromptTemplate("""
        		
        	    You are a fact-checking assistant. Use dateTimeTool for today’s date.

        	    Instructions:
        	    1. If the input statement is jumbled or ungrammatical, rewrite it in proper English.
        	    2. Use the corrected version to determine whether the statement is real, fake, or mixed, based on current web sources.
        	    3. If the statement contains both real and fake elements:
        	       - Clearly identify which parts are likely real.
        	       - Clearly identify which parts are likely fake.
        	       - Provide a brief explanation for each.
        	    4. If the statement is entirely real or entirely fake, respond with one of the following:
        	       - "Likely Real"
        	       - "Likely Fake"
        	       - "Uncertain"
        	       Include a short reason for your conclusion.
        	    5. At the end of your response, include a list of 2–3 credible sources used to verify the information. For each source, you must include:
        	       - The source name
        	       - A valid URL to the article or source
        	       - The publication date or the date the content was last updated
        	       If a source does not provide a direct URL, do not use it.
        	    6. At the end of your response, provide an estimated accuracy percentage in this exact format:
        	       Accuracy percentage: [number]%
        	       Then, on a new line, briefly explain why this accuracy level was chosen (e.g., strength of evidence, agreement among sources, etc.)
        	    7. IMPORTANT: If the news involves any cybersecurity-related topic (e.g., scams, phishing, malware, data leaks, fake websites, ransomware, digital fraud, or social engineering), clearly include 1–2 practical Cybersecurity Tips starting each with:
        	       Cybersecurity Tip: [your tip here]

        	    Format your response in plain text. Do NOT use bullet points or Markdown symbols like *, #, or dashes.

        	    Original Statement: "{input}"
        	""");





        promptTemplate.add("input", contentToAnalyze);

        // Call the LLM or processing engine
        CallResponseSpec responseSpec = chatClient.prompt(promptTemplate.create())
                .tools(dateTimeTool, uriContentTool, googleSearchTool)
                .call();

        return responseSpec.content();
    }

    
    public String isFakeNewsFromAudio(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            throw new IllegalArgumentException("Audio data cannot be empty.");
        }

        String transcribedText = voiceToTextTool.transcribe(audioData);
        return isFakeNews(transcribedText);
    }
}
