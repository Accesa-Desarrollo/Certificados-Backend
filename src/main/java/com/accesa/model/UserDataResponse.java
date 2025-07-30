package com.accesa.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDataResponse {

	@NotBlank
	private String fullName;

	@NotBlank
	private String identityCard;
	
	@NotBlank
	private String hireDate;
	
	@NotBlank
	private String jobPosition;
	
	@NotBlank
	private String site;

	private String workHours;
	
	private String salary;

}
