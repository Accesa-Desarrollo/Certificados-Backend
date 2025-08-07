package com.accesa.service;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.sql.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.accesa.exception.DatabaseException;
import com.accesa.exception.GenerationException;
import com.accesa.exception.NotFoundException;
import com.accesa.model.UserDataRequest;
import com.accesa.model.UserDataResponse;

import lombok.extern.slf4j.Slf4j;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class EmploymentCertificateService {

	@Value("${db.oracle.driver-class-name}")
	private String DRIVER_ORACLE = "";
	@Value("${db.oracle.url}")
	private String PROTOCOL_PORTAL = "";
	@Value("${db.oracle.username}")
	private String USER_PORTAL = "";
	@Value("${db.oracle.password}")
	private String PASSWORD_PORTAL = "";

	@Value("${db.sqlserver.driver-class-name}")
	private String DRIVER_GIRH = "";
	@Value("${db.sqlserver.url}")
	private String PROTOCOL_GIRH = "";
	@Value("${db.sqlserver.username}")
	private String USER_GIRH = "";
	@Value("${db.sqlserver.password}")
	private String PASSWORD_GIRH = "";

	@Value("${header.image}")
	private String HEADER_IMAGE = "";
	@Value("${footer.image}")
	private String FOOTER_IMAGE = "";

	public UserDataResponse getUserData(String id) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		UserDataResponse userData = null;
		try {
			Class.forName(DRIVER_ORACLE).getDeclaredConstructor().newInstance();
		} catch (Exception e1) {
			throw new DatabaseException("oracle loading issues found " + e1.getMessage(), e1);
		}

		String sql = "SELECT * from(SELECT name, LASTNAME, SECONDLASTNAME,DOCUMENT ,ENTRYDATE ,CHARGE ,SITE, pdd.HORDEFIN "
				+ "FROM HUMANRESOURCES_STAFF hs JOIN PARTE_DIARIO_DETALLE pdd ON hs.document = pdd.cedula "
				+ "WHERE hs.code ='" + id + "' " + "ORDER BY pdd.HORDEFIN  DESC" + ") WHERE rownum = 1";

		try (Connection connection = DriverManager.getConnection(PROTOCOL_PORTAL, USER_PORTAL, PASSWORD_PORTAL);
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery(sql)) {

			if (result.next()) {
				userData = new UserDataResponse();
				LocalDate entryDate = LocalDate.parse(result.getString("entrydate"));

				String formattedDate = entryDate.format(formatter);
				userData.setHireDate(formattedDate);
				userData.setIdentityCard(String.valueOf(result.getString("document")));
				userData.setJobPosition(result.getString("charge"));
				userData.setSite(result.getString("site"));

				String name = result.getString("name").substring(0, 1).toUpperCase()
						+ result.getString("name").substring(1);
				String lastName = result.getString("lastname").substring(0, 1).toUpperCase()
						+ result.getString("lastname").substring(1);
				String[] palabras = result.getString("secondlastname").toLowerCase().split(" ");
		        StringBuilder secondLastNameFormatted = new StringBuilder();

		        for (String palabra : palabras) {
		            if (!palabra.isEmpty()) {
		                secondLastNameFormatted.append(Character.toUpperCase(palabra.charAt(0)))
		                         .append(palabra.substring(1))
		                         .append(" ");
		            }
		        }
				userData.setFullName(name + " " + lastName + " " + secondLastNameFormatted);
				String hordefinFormatted = result.getString("hordefin").replace("-", " a ");
				userData.setWorkHours(hordefinFormatted);
			}

		} catch (SQLException e) {
			log.info("{}", "error retrieving user data from portal" + e.getMessage());
			throw new DatabaseException("error retrieving user data from portal" + e.getMessage(), e);
		}

		if (userData != null) {
			try {
				Class.forName(DRIVER_GIRH).getDeclaredConstructor().newInstance();
			} catch (Exception e2) {
				throw new DatabaseException("SQLServer loading issues found " + e2.getMessage(), e2);
			}

			String sqlGirh = "SELECT TOP 1 CAST(hisImp AS DECIMAL(10,2)) salary FROM HISTORIC WHERE HisConCod=803 AND HisFunCod = "+userData.getIdentityCard()+" ORDER BY HisLiqFch desc, hisImp DESC\r\n"
					+ "";
			try (Connection connectionGirh = DriverManager.getConnection(PROTOCOL_GIRH, USER_GIRH, PASSWORD_GIRH);
					Statement statementGirh = connectionGirh.createStatement();
					ResultSet resultGirh = statementGirh.executeQuery(sqlGirh)) {

				if (resultGirh.next()) {
					userData.setSalary(resultGirh.getString("salary"));
				}
			} catch (SQLException e) {
				log.info("{}", "error retrieving user data from girh" + e.getMessage());
				throw new DatabaseException("error retrieving user data from girh" + e.getMessage(), e);
			}
		}

		return userData;

	}

	public byte[] generateCertificate(UserDataRequest request) {
		LocalDate today = LocalDate.now();

		String city = "Montevideo";
		int day = today.getDayOfMonth();
		int month = today.getMonthValue();
		int year = today.getYear();
		
		String dayFormatted = String.format("%02d", day);
		String monthFormatted = String.format("%02d", month);

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Document document = new Document();

			Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
			Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12);

			PdfWriter.getInstance(document, out);
			document.open();

			Image headerImage = Image.getInstance(HEADER_IMAGE);
			headerImage.scaleToFit(500, 50);
			headerImage.setAlignment(Element.ALIGN_LEFT);
			document.add(headerImage);

			Paragraph date = new Paragraph();
			date.add(new Chunk(city + ", " + dayFormatted + " de " + monthFormatted + " de " + year, italicFont));
			date.setAlignment(Element.ALIGN_RIGHT);
			date.setSpacingAfter(10);
			document.add(date);

			Paragraph whoever = new Paragraph();
			whoever.add(new Chunk("A quien corresponda:", italicFont));
			whoever.setAlignment(Element.ALIGN_LEFT);
			whoever.setSpacingAfter(10);
			document.add(whoever);

			Paragraph message = new Paragraph(
					"Se deja constancia que el/la Sr./Sra. " + request.getFullName() + " con cédula de identidad "
							+ request.getIdentityCard() + " " + "trabaja en ACCESA S.A. desde la fecha "
							+ request.getHireDate() + " del ingreso y se desempeña actualmente en el cargo de "
							+ request.getJobPosition() + ", " + "en el sitio " + request.getSite() + ". ",
					italicFont);
			message.setAlignment(Element.ALIGN_LEFT);

			Chunk workload = null;
			Chunk salary = null;
			if (request.getWorkHours() != null && !request.getWorkHours().isBlank()) {
				workload = new Chunk("En el horario de " + request.getWorkHours() + ". ", italicFont);
			}
			if (request.getSalary() != null && !request.getSalary().isBlank()) {
				salary = new Chunk("Con el sueldo de " + request.getSalary() + ". ", italicFont);
			}

			message.add(workload);
			message.add(salary);
			message.setSpacingAfter(10);
			document.add(message);

			Paragraph confirm = new Paragraph("Se expide la misma, a expresa solicitud de la parte interesada.",
					italicFont);
			confirm.setAlignment(Element.ALIGN_LEFT);
			confirm.setSpacingAfter(10);
			document.add(confirm);

			Paragraph ending = new Paragraph("Sin otro particular, saluda cordialmente.", italicFont);
			ending.setAlignment(Element.ALIGN_LEFT);
			ending.setSpacingAfter(10);
			document.add(ending);
			Paragraph department = new Paragraph("Capital Humano", boldFont);
			department.setAlignment(Element.ALIGN_LEFT);
			document.add(department);
			Paragraph company = new Paragraph("ACCESA S.A.", boldFont);
			company.setAlignment(Element.ALIGN_LEFT);
			document.add(company);

			Image footerImage;

			footerImage = Image.getInstance(FOOTER_IMAGE);
			footerImage.scaleToFit(500, 50);
			footerImage.setAlignment(Element.ALIGN_LEFT);
			document.add(footerImage);

			document.close();

			return out.toByteArray();
		} catch (DocumentException e) {
			throw new GenerationException("error generating PDF file", e);
		} catch (IOException e2) {
			throw new GenerationException("error loading header image", e2);
		}
	}

	public void userExists(String id) {

		try {
			Class.forName(DRIVER_ORACLE).getDeclaredConstructor().newInstance();
		} catch (Exception e1) {
			throw new DatabaseException("oracle loading issues found " + e1.getMessage(), e1);
		}

		String sql = "SELECT * FROM HUMANRESOURCES_STAFF hs WHERE hs.document ='" + id + "'";

		try (Connection connection = DriverManager.getConnection(PROTOCOL_PORTAL, USER_PORTAL, PASSWORD_PORTAL);
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery(sql)) {

			if (!result.next()) {
				throw new NotFoundException("Usuario " + id + " no encontrado");
			}

		} catch (SQLException e) {
			log.info("{}", "error retrieving user data from portal" + e.getMessage());
			throw new DatabaseException("error retrieving user data from portal" + e.getMessage(), e);
		}

	}

}
