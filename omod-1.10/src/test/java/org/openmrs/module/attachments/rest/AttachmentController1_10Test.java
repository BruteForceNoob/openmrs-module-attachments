package org.openmrs.module.attachments.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.beans.factory.annotation.Autowired;

public class AttachmentController1_10Test extends MainResourceControllerTest {

	@Autowired
	protected ObsService obsService;

	@Autowired
	protected TestHelper testHelper;

	private Obs obs;

	@Before
	public void setup() throws IOException {
		obs = testHelper.getTestComplexObs();
	}

	@Override
	public String getURI() {
		return AttachmentsConstants.ATTACHMENT_URI;
	}

	@Override
	public String getUuid() {
		return obs.getUuid();
	}

	@Override
	public long getAllCount() {
		return 0;
	}

	@Override @Test public void shouldGetAll() {}
	@Override @Test public void shouldGetDefaultByUuid() {}
	@Override @Test public void shouldGetRefByUuid() {}
	@Override @Test public void shouldGetFullByUuid() {}

	@Test
	public void saveVisitDocument_shouldUpdateObsComment() throws Exception {
		// Setup
		String editedComment = "Hello world!";
		String json = "{\"uuid\":\"" + getUuid() + "\",\"comment\":\"" + editedComment + "\"}";

		// Replay
		Object doc = deserialize( handle( newPostRequest(getURI() + "/" + getUuid(), json) ) );

		// Verif
		String comment = (String) PropertyUtils.getProperty(doc, "comment");
		assertEquals(editedComment, comment);
	}
	
	@Test
	public void deleteVisitDocument_shouldVoidObs() throws Exception {
		// Setup
		File file = new File(testHelper.getTestComplexObsFilePath());

		// Replay
		handle( newDeleteRequest(getURI() + "/" + getUuid()) );

		// Verif
		assertTrue(obsService.getObsByUuid(getUuid()).isVoided());
	}

	@Test
	public void deleteVisitDocumentWithMissingFile_shouldVoidObs() throws Exception {
		// Setup
		File file = new File(testHelper.getTestComplexObsFilePath());
		file.delete();
		assertFalse(file.exists());

		// Replay
		handle( newDeleteRequest(getURI() + "/" + getUuid()) );

		// Verif
		assertTrue(obsService.getObsByUuid(getUuid()).isVoided());
	}

	@Test
	public void purgeVisitDocument_shouldPurgeObsAndRemoveFile() throws Exception {
		// Setup
		File file = new File(testHelper.getTestComplexObsFilePath());
		assertTrue(file.exists());

		// Replay
		handle( newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")) );

		// Verif
		assertNull(obsService.getObsByUuid(getUuid()));
		assertFalse(file.exists());
	}

	@Test
	public void purgeVisitDocumentWithMissingFile_shouldPurgeObs() throws Exception {
		// Setup
		File file = new File(testHelper.getTestComplexObsFilePath());
		file.delete();
		assertFalse(file.exists());

		// Replay
		handle( newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")) );

		// Verif
		assertNull(obsService.getObsByUuid(getUuid()));
	}

	@Test
	public void deleteVisitDocument_shouldVoidObsWithoutAssociatedEncounter() throws Exception {
		// Setup
		Obs obs = testHelper.getTestComplexObsWithoutAssociatedEncounterOrVisit();

		// Replay
		handle( newDeleteRequest(getURI() + "/" + obs.getUuid()) );

		// Verify
		assertTrue(obsService.getObsByUuid(obs.getUuid()).isVoided());
	}

	@Test
	public void purgeVisitDocument_shouldPurgeObsWithoutAssociatedEncounter() throws Exception {
		// Setup
		Obs obs = testHelper.getTestComplexObsWithoutAssociatedEncounterOrVisit();

		// Replay
		handle( newDeleteRequest(getURI() + "/" + obs.getUuid(), new Parameter("purge", "")) );

		// Verify
		assertNull(obsService.getObsByUuid(obs.getUuid()));
	}
}