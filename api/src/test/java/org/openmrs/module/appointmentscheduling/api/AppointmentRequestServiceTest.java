package org.openmrs.module.appointmentscheduling.api;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointmentscheduling.AppointmentRequest;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class AppointmentRequestServiceTest extends BaseModuleContextSensitiveTest {

    private static final int TOTAL_APPOINTMENT_REQUESTS = 3;

    private static final int TOTAL_NON_VOIDED_APPOINTMENT_REQUESTS = 2;

    @Autowired
    private AppointmentService service;

    @Autowired
    LocationService locationService;

    @Autowired
    ProviderService providerService;

    @Autowired
    PatientService patientService;

    @Before
    public void before() throws Exception {
        executeDataSet("standardAppointmentTestDataset.xml");
    }

    @Test
    @Verifies(value = "should get all appointment requests", method = "getAllAppointmentRequests()")
    public void getAllAppointmentRequests_shouldGetAllAppointmentRequests() throws Exception {
        List<AppointmentRequest> appointmentRequests = service.getAllAppointmentRequests();
        assertEquals(TOTAL_APPOINTMENT_REQUESTS, appointmentRequests.size());
    }

    @Test
    @Verifies(value = "should get correct appointment request", method = "getAppointmentRequest(Integer)")
    public void getAppointmentRequest_shouldGetCorrectAppointmentRequest() throws Exception {

        AppointmentRequest appointmentRequest = service.getAppointmentRequest(1);
        assertNotNull(appointmentRequest);
        assertEquals("862c94f0-3dae-11e4-916c-0800200c9a66", appointmentRequest.getUuid());
        assertEquals(providerService.getProvider(1), appointmentRequest.getProvider());
        assertEquals(patientService.getPatient(2), appointmentRequest.getPatient());
        assertEquals(service.getAppointmentType(1), appointmentRequest.getAppointmentType());
        assertEquals(AppointmentRequest.AppointmentRequestStatus.PENDING, appointmentRequest.getStatus());
        assertEquals(providerService.getProvider(1), appointmentRequest.getRequestedBy());
        assertEquals("ASAP", appointmentRequest.getNotes());

        appointmentRequest = service.getAppointmentRequest(2);
        assertNotNull(appointmentRequest);
        assertEquals("862c94f1-3dae-11e4-916c-0800200c9a66", appointmentRequest.getUuid());
        assertEquals(providerService.getProvider(1), appointmentRequest.getProvider());
        assertEquals(patientService.getPatient(6), appointmentRequest.getPatient());
        assertEquals(service.getAppointmentType(2), appointmentRequest.getAppointmentType());
        assertEquals(AppointmentRequest.AppointmentRequestStatus.FULFILLED, appointmentRequest.getStatus());
        assertEquals(providerService.getProvider(2), appointmentRequest.getRequestedBy());
        assertNull(appointmentRequest.getNotes());

    }

    @Test
    @Verifies(value = "should get correct appointment request", method = "getAppointmentRequestByUuid(String)")
    public void getAppointmentRequestByUuid_shouldGetCorrectAppointmentRequest() throws Exception {

        AppointmentRequest appointmentRequest = service.getAppointmentRequestByUuid("862c94f2-3dae-11e4-916c-0800200c9a66");
        assertNotNull(appointmentRequest);
        assertEquals(new Integer(3), appointmentRequest.getId());
    }

    @Test
    @Verifies(value = "should save new appointment request", method = "saveAppointmentRequest(AppointmentRequest)")
    public void saveAppointmentRequest_shouldSaveNewAppointmentRequest() throws Exception {

        List<AppointmentRequest> appointmentRequests = service.getAllAppointmentRequests(true);
        assertEquals(TOTAL_APPOINTMENT_REQUESTS, appointmentRequests.size());

        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setPatient(patientService.getPatient(2));
        appointmentRequest.setAppointmentType(service.getAppointmentType(1));
        appointmentRequest.setProvider(providerService.getProvider(1));
        appointmentRequest.setRequestedBy(providerService.getProvider(1));
        appointmentRequest.setRequestedOn(new Date());
        appointmentRequest.setNotes("test");
        appointmentRequest.setStatus(AppointmentRequest.AppointmentRequestStatus.PENDING);
        service.saveAppointmentRequest(appointmentRequest);

        //Should create a new appointment request row
        assertEquals(TOTAL_APPOINTMENT_REQUESTS + 1, service.getAllAppointmentRequests().size());
    }

    @Test
    @Verifies(value = "should save new appointment request", method = "saveAppointmentRequest(AppointmentRequest)")
    public void saveAppointmentRequest_shouldSaveNewAppointmentRequestWithMinimalParameters() throws Exception {

        List<AppointmentRequest> appointmentRequests = service.getAllAppointmentRequests(true);
        assertEquals(TOTAL_APPOINTMENT_REQUESTS, appointmentRequests.size());

        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setPatient(patientService.getPatient(2));
        appointmentRequest.setAppointmentType(service.getAppointmentType(1));
        appointmentRequest.setRequestedOn(new Date());
        appointmentRequest.setStatus(AppointmentRequest.AppointmentRequestStatus.PENDING);
        service.saveAppointmentRequest(appointmentRequest);

        appointmentRequest = service.getAppointmentRequest(4);
        assertNotNull(appointmentRequest);

        //Should create a new appointment request row
        assertEquals(TOTAL_APPOINTMENT_REQUESTS + 1, service.getAllAppointmentRequests().size());
    }


    @Test
    @Verifies(value = "should save edited appointment request", method = "saveAppointmentRequest(AppointmentRequest)")
    public void saveAppointmentRequest_shouldSaveEditedAppointmentRequest() throws Exception {

        AppointmentRequest appointmentRequest = service.getAppointmentRequest(1);
        appointmentRequest.setPatient(patientService.getPatient(6));

        service.saveAppointmentRequest(appointmentRequest);

        appointmentRequest = service.getAppointmentRequest(1);
        assertEquals(patientService.getPatient(6), appointmentRequest.getPatient());

        //Should not change the number of appointment types
        assertEquals(TOTAL_APPOINTMENT_REQUESTS, service.getAllAppointmentRequests().size());
    }

    @Test
    @Verifies(value = "should void given appointment request", method = "voidAppointmentRequest(AppointmentRequest, String)")
    public void voidAppointmentRequest_shouldVoidGivenAppointmentRequest() throws Exception {

        // sanity check
        assertEquals(TOTAL_NON_VOIDED_APPOINTMENT_REQUESTS, service.getAllAppointmentRequests(false).size());

        AppointmentRequest appointmentRequest = service.getAppointmentRequest(1);
        service.voidAppointmentRequest(appointmentRequest, "test");

        // one less on non-voided appointment request
        assertEquals(TOTAL_NON_VOIDED_APPOINTMENT_REQUESTS - 1, service.getAllAppointmentRequests(false).size());

        // still same total count, however,
        assertEquals(TOTAL_APPOINTMENT_REQUESTS, service.getAllAppointmentRequests().size());

        appointmentRequest = service.getAppointmentRequest(1);
        assertTrue(appointmentRequest.isVoided());

    }

    @Test
    @Verifies(value = "should unvoid given appointment request", method = "unvoidAppointmentRequest(AppointmentRequest)")
    public void unvoidAppointmentRequest_shouldUnvoidGivenAppointmentRequest() throws Exception {

        // sanity check
        assertEquals(TOTAL_NON_VOIDED_APPOINTMENT_REQUESTS, service.getAllAppointmentRequests(false).size());

        AppointmentRequest appointmentRequest = service.getAppointmentRequest(3);
        service.unvoidAppointmentRequest(appointmentRequest);

        // one more non-voided appointment request
        assertEquals(TOTAL_NON_VOIDED_APPOINTMENT_REQUESTS + 1, service.getAllAppointmentRequests(false).size());

        // still same total count, however,
        assertEquals(TOTAL_APPOINTMENT_REQUESTS, service.getAllAppointmentRequests().size());

        appointmentRequest = service.getAppointmentRequest(3);
        assertFalse(appointmentRequest.isVoided());

    }

    @Test
    @Verifies(value = "should delete given appointment request", method = "purgeAppointmentRequest(AppointmentRequest)")
    public void purgeAppointmentRequest_shouldDeleteGivenAppointmentRequest() throws Exception {

        AppointmentRequest appointmentRequest = service.getAppointmentRequest(3);
        assertNotNull(appointmentRequest);

        service.purgeAppointmentRequest(appointmentRequest);

        appointmentRequest = service.getAppointmentRequest(3);
        assertNull(appointmentRequest);

        //Should decrease the number of appointment requestss by one.
        assertEquals(TOTAL_APPOINTMENT_REQUESTS - 1, service.getAllAppointmentRequests().size());
    }

}
