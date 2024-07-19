package com.kuailu.im.server.service.impl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class OpenAiChatModelExamples {

        /**
        public static void main(String[] args) {

            ChatLanguageModel model = OpenAiStreamingLanguageModel.builder()
                    .baseUrl("http://192.168.204.198:8000/v1/")
                    .apiKey("IGNORE")
                    .build();

            String prompt = "你好";
            String joke = model.generate(prompt);

            System.out.println(joke);
        }
         */



    public static void main(String[] args){
        /**
        StreamingLanguageModel model2 = OpenAiStreamingLanguageModel.builder()
                .baseUrl("http://192.168.204.198:8000/v1/")
                .apiKey("IGNORE")
                .organizationId(System.getenv("ORGANIZATION_ID"))
                .logRequests(true)
                .logResponses(true)
                .build();
        */
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                .baseUrl("http://192.168.204.198:8000/v1/")
                .apiKey("IGNORE")
                .organizationId("OPENAI_ORGANIZATION_ID")
                .user("user id")
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .build();

        String prompt = "能介绍一下中国宋朝历史吗";
        CompletableFuture<String> futureAnswer = new CompletableFuture<>();
        CompletableFuture<Response<AiMessage>> futureResponse = new CompletableFuture<>();

        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        ChatMessage message = new UserMessage (prompt);
        messages.add(message);

        model.generate(messages, new StreamingResponseHandler<AiMessage>() {

            private final StringBuilder answerBuilder = new StringBuilder();

            @Override
            public void onNext(String token) {
                System.out.println("onNext: '" + token + "'");
                answerBuilder.append(token);
                System.out.println("answer: " + answerBuilder.toString());
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                System.out.println("onComplete: '" + response + "'");
                futureAnswer.complete(answerBuilder.toString());
                futureResponse.complete(response);
            }

            @Override
            public void onError(Throwable error) {
                futureAnswer.completeExceptionally(error);
                futureResponse.completeExceptionally(error);
            }
        });

    }



}
