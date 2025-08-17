package com.example.wordguessinggame;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

// Retrofit interface for word-related API endpoints
public interface ApiService {

    // Fetch random word from API
    @Headers("X-Api-Key: " + BuildConfig.API_KEY) // API key injected from build config
    @GET("v1/randomword")
    Call<RandomWordResponse> getRandomWord();

    // Get synonyms for specified word
    @Headers("X-Api-Key: " + BuildConfig.API_KEY)
    @GET("v1/thesaurus")
    Call<ThesaurusResponse> getSynonyms(@Query("word") String word); // Word to find synonyms for
}