import static org.junit.Assert.assertEquals;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.junit.Test;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.IGenericClient;

public class ExampleTest {
    @Test
    public void testExample() {
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = "http://fhirtest.uhn.ca/baseDstu2";//"http://test.aidbox.local/fhir"; //

        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        Patient patient = new Patient();
        patient.addIdentifier().setSystem("urn:system").setValue("value-aidbox");
        patient.addName().addFamily("Aidbox").addGiven("John");

        MethodOutcome outcome = client.create()
                .resource(patient)
                .conditional()
                .where(Patient.IDENTIFIER.exactly().systemAndIdentifier("urn:system", "value-aidbox"))
                .encodedJson()
                .execute();

        Boolean created = outcome.getCreated();

        IdDt id = (IdDt) outcome.getId();

        Bundle patients = client
                .search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value("Aidbox"))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .execute();

        assertEquals("Must be 1 patient named 'Aidbox'", 1, patients.getEntry().size());

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
}
