package com.accesa.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.accesa.model.UserDataRequest;
import com.accesa.model.UserDataResponse;
import com.accesa.service.EmploymentCertificateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.POST, RequestMethod.GET })
@RequestMapping("/api")
@Tag(name = "Employee Certificates")
public class EmploymentCertificateController {

	private EmploymentCertificateService employmentCertificateService;

	public EmploymentCertificateController(EmploymentCertificateService employmentCertificateService) {
		this.employmentCertificateService = employmentCertificateService;
		// TODO Auto-generated constructor stub
	}

	@Operation(summary = "Get user data")
	@Parameter(name = "id", description = "User Id", example = "C991991", required = true)
	@ApiResponse(responseCode = "200", description = "Successful response")
	@ApiResponse(responseCode = "204", description = "No user information")
	@GetMapping("/users/{id}")
	public ResponseEntity<?> getUserData(@PathVariable("id") String id) {

		UserDataResponse data = employmentCertificateService.getUserData(id);

		if (data == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}

		return ResponseEntity.status(HttpStatus.OK).body(data);

	}

	@Operation(summary = "Generate an employment certificate", 
			description = "given default/ optional attributes from a user, gives the possibility to generate an employment certificate.", 
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The user data set", required = true))
	@ApiResponse(responseCode = "200", description = "Successful response")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@ApiResponse(responseCode = "404", description = "User not found")
	@PostMapping("/certificates")
	public ResponseEntity<?> generateCertificate(@Valid @RequestBody UserDataRequest request) {
		
		employmentCertificateService.userExists(request.getIdentityCard());

		byte[] pdf = employmentCertificateService.generateCertificate(request);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=certificado_laboral_" + request.getIdentityCard() + ".pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdf);
	}

}
