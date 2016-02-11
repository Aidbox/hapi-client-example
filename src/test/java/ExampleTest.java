import static org.junit.Assert.assertEquals;

import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import org.junit.Test;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.IGenericClient;

public class ExampleTest {
    @Test
    public void testExample() {
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = "http://fhirtest.uhn.ca/baseDstu2";//"http://test.aidbox.local/fhir"; //"http://hapi.devbox.health-samurai.io/fhir";//

        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        System.out.println("Patient");
        Patient patient = new Patient();
        patient.addIdentifier().setSystem("urn:system").setValue("value-aidbox");
        patient.addName().addFamily("Aidbox").addGiven("John");

        MethodOutcome outcome = client.create()
                .resource(patient)
                .conditional()
                .where(Patient.IDENTIFIER.exactly().systemAndIdentifier("urn:system", "value-aidbox"))
                .encodedJson()
                .execute();

        IdDt id = (IdDt) outcome.getId();

        Bundle patients = client
                .search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value("Aidbox"))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .execute();

        assertEquals("Must be 1 patient named 'Aidbox'", 1, patients.getEntry().size());

        System.out.println("MedicationAdministration");
        MedicationAdministration medication = new MedicationAdministration();
        medication.setPatient(new ResourceReferenceDt(id));

        client.create()
                .resource(medication)
                .conditional()
                .where(MedicationAdministration.PATIENT.hasId(id))
                .encodedJson()
                .execute();

        Bundle medications = client
                .search()
                .forResource(MedicationAdministration.class)
                .where(MedicationAdministration.PATIENT.hasId(id))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .execute();

        assertEquals("Must be 1 medication for patient named 'Aidbox'", 1, medications.getEntry().size());

        for (Bundle.Entry e : medications.getEntry()) {
            client.delete().resourceById(e.getResource().getId()).execute();
        }
        client.delete().resourceById(id).execute();
    }

    @Test
    public void testDemo() {
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = "http://fhirtest.uhn.ca/baseDstu2";//"http://test.aidbox.local/fhir"; //"http://hapi.devbox.health-samurai.io/fhir";//

        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        //BasicAuthInterceptor authInterceptor = new BasicAuthInterceptor("Username", "Password");
        //client.registerInterceptor(authInterceptor);

        IdentifierDt patientIdentifier = new IdentifierDt("urn:io.healthsamurai:unittest:patient","testdata");
        IdentifierDt medicationIdentifier = new IdentifierDt("urn:cio.healthsamurai:unittest:medadmin","testdata");

        Patient patient = new Patient();
        patient.addIdentifier(patientIdentifier);
        patient.addName().addFamily("Smith").addGiven("John");

        MethodOutcome outcome = client.create()
                .resource(patient)
                .conditional()
                .where(Patient.IDENTIFIER.exactly().identifier(patientIdentifier))
                .encodedJson()
                .execute();

        IdDt idPatient = (IdDt) outcome.getId();

        MedicationAdministration medication = new MedicationAdministration();
        medication.addIdentifier(medicationIdentifier);
        medication.setPatient(new ResourceReferenceDt(idPatient));

        client.create()
                .resource(medication)
                .conditional()
                .where(MedicationAdministration.PATIENT.hasId(idPatient))
                .encodedJson()
                .execute();

        Bundle patientBundle = client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().identifier(patientIdentifier))
                .returnBundle(Bundle.class).execute();

        Bundle medicationBundle = client.search()
                .forResource(MedicationAdministration.class)
                .where(MedicationAdministration.IDENTIFIER.exactly().identifier(medicationIdentifier))
                .returnBundle(Bundle.class).execute();

        assertEquals("Must be 1 patient", 1, patientBundle.getEntry().size());
        assertEquals("Must be 1 medication", 1, medicationBundle.getEntry().size());

        for(Bundle.Entry entry : medicationBundle.getEntry()) {
            client.delete().resource(entry.getResource()).execute();
        }

        for(Bundle.Entry entry : patientBundle.getEntry()) {
            client.delete().resource(entry.getResource()).execute();
        }
    }
}
