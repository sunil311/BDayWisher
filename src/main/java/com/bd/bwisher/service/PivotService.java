package com.bd.bwisher.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.bd.bwisher.beans.BirthdayEmail;
import com.bd.bwisher.beans.config.BDayConfiguration;
import com.bd.exceptions.BWisherException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** This Class interact with Pivot service for getting data. */
public class PivotService {

	@Autowired
	private BDayConfiguration applicationConfiguration;
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Getting Employee data from pivot for a specific day.
	 * 
	 * @param date
	 * @return
	 */
	public List<BirthdayEmail> getEmployees(Date date) throws BWisherException {
		try {

			// readDataFromFileSystem();
			String webEndPoint = "http://pivot.impetus.co.in:8088/wishes/getUsers";
			RestTemplate restTemplate = new RestTemplate();
			ObjectNode request = mapper.createObjectNode();
			ArrayNode locations = mapper.createArrayNode();
			locations.add("Noida");
			locations.add("Gurgaon");
			request.set("location", locations);
			
			ResponseEntity<ArrayNode> responseEntity = restTemplate.postForEntity(webEndPoint, request, ArrayNode.class);
			ArrayNode jsonArray = responseEntity.getBody();
			List<BirthdayEmail> employees = new ArrayList<BirthdayEmail>();
			/*ResponseEntity<ArrayNode> responseEntity = restTemplate.getForEntity(
					webEndPoint, ArrayNode.class);
			ArrayNode jsonArray = responseEntity.getBody();
			
			for(int i =0; i < jsonArray.size(); i++){
				JsonNode jsonNode = jsonArray.get(i);
				BirthdayEmail birthdayEmail = new BirthdayEmail();
				//birthdayEmail.setEMAIL(jsonNode.get("EMAIL").asText());
				birthdayEmail.setEMAIL(jsonNode.get("EMAIL").asText());
				birthdayEmail.setNAME(jsonNode.get("NAME").asText());
				birthdayEmail.setIMGURL(jsonNode.get("IMGURL").asText());
				birthdayEmail.setSUBJECT(jsonNode.get("SUBJECT").asText());
				employees.add(birthdayEmail);
			}*/
			BirthdayEmail birthdayEmail = new BirthdayEmail();
			//birthdayEmail.setEMAIL(jsonNode.get("EMAIL").asText());
			birthdayEmail.setEMAIL("sunil.gupta@impetus.co.in");
			birthdayEmail.setNAME("Sunil");
			birthdayEmail.setIMGURL("https://pivot.impetus.co.in/digite/upload/skgupta.jpg");
			birthdayEmail.setSUBJECT("Birthday");
			employees.add(birthdayEmail);
			return employees;

		} catch (Exception e) {
			throw new BWisherException("Pivot Service Exception occured.", e);
		}
	}

	public static void main(String args[]){
		PivotService pivotService = new PivotService();
		pivotService.getEmployees(new Date());
	}
	
	private ArrayNode readDataFromFileSystem() throws JsonProcessingException,
			IOException {
		ArrayNode arrayNode = (ArrayNode) mapper.readTree(new String(Files
				.readAllBytes(Paths.get(ClassLoader.getSystemResource(
						"data.json").getFile())), StandardCharsets.UTF_8));
		return arrayNode;
	}
}
