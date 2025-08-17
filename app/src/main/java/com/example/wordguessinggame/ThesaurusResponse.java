package com.example.wordguessinggame;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Models thesaurus API response structure
public class ThesaurusResponse {
    @SerializedName("synonyms") // Maps JSON field to list
    public List<String> synonyms; // Contains all synonyms for queried word
}