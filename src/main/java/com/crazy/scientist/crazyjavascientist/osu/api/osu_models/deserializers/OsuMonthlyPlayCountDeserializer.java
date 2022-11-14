package com.crazy.scientist.crazyjavascientist.osu.api.osu_models.deserializers;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuApiEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuBestPlayEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.OsuMonthlyPlaycountsModel;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
public class OsuMonthlyPlayCountDeserializer  {





    public List<OsuMonthlyPlaycountsModel> deserialize(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode arrayNode = mapper.readTree(json).get("monthly_playcounts");

        List<OsuMonthlyPlaycountsModel> playcountsList = new ArrayList<>();

        if(arrayNode.isArray()){
            for(JsonNode objNode: arrayNode){
                playcountsList.add(new OsuMonthlyPlaycountsModel(objNode.get("start_date").asText(), objNode.get("count").asInt()));
            }
        }

        return playcountsList;
    }

}
