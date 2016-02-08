import static org.junit.Assert.assertEquals;

import org.junit.Test;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.IGenericClient;

public class PatientTest {
    @Test
    public void testPatient() {
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = "http://fhirtest.uhn.ca/baseDstu2";

        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value("duck"))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .execute();

        assertEquals("Must be 10 patients named 'duck'", 10, results.getEntry().size());
    }
}