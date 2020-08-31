/*
 *
 *  *
 *  * Copyright 2020 First People's Cultural Council
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  * /
 *
 */

package ca.firstvoices.characters.services;

import static ca.firstvoices.data.schemas.DialectTypesConstants.FV_CHARACTER;
import static org.junit.Assert.assertNotNull;

import ca.firstvoices.testUtil.AbstractFirstVoicesDataTest;
import ca.firstvoices.testUtil.FirstVoicesDataFeature;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features({FirstVoicesDataFeature.class})
@Deploy({
    "FirstVoicesCharacters:OSGI-INF/services/addConfusables-contrib.xml",
    "FirstVoicesCharacters:OSGI-INF/services/cleanupCharacters-contrib.xml"
})
public class AddConfusablesServiceImplTest extends AbstractFirstVoicesDataTest {

  AddConfusablesService addConfusablesService = Framework.getService(AddConfusablesService.class);

  @Test
  public void addConfusablesTest() {
    String[] confusablesToAdd = {"￠", "ȼ"};

    assertNotNull("Dialect cannot be null", dialect);
    String path = dialect.getPathAsString();

    DocumentModel testConfusable = createDocument(session,
        session.createDocumentModel(path, "TestChar", FV_CHARACTER));
    testConfusable.setPropertyValue("dc:title", "¢");
    session.saveDocument(testConfusable);

    DocumentModel updated = session.saveDocument(addConfusablesService
        .updateConfusableCharacters(session, testConfusable, dialect, "¢", confusablesToAdd));

    String[] property = (String[]) updated.getPropertyValue("fvcharacter:confusable_characters");

    Assert.assertTrue(property[0].equals(confusablesToAdd[0]));
    Assert.assertTrue(property[1].equals(confusablesToAdd[1]));

    DocumentModel testConfusableUppercase = createDocument(session,
        session.createDocumentModel(path, "TestChar", FV_CHARACTER));
    testConfusableUppercase.setPropertyValue("fvcharacter:upper_case_character", "¢");

    session.saveDocument(testConfusableUppercase);

    updated = session.saveDocument(addConfusablesService
        .updateConfusableCharacters(session, testConfusableUppercase, dialect, "¢",
            confusablesToAdd));

    String[] uProperty = (String[]) updated
        .getPropertyValue("fvcharacter:upper_case_confusable_characters");

    Assert.assertTrue(uProperty[0].equals(confusablesToAdd[0]));
    Assert.assertTrue(uProperty[1].equals(confusablesToAdd[1]));
  }

  @Test
  public void ignoreConfusablesAlreadyUsed() {
    String[] confusablesToAdd = {"ā", "a̅", "ā", "ā", "a¯", "a‾"};

    assertNotNull("Dialect cannot be null", dialect);
    String path = alphabet.getPathAsString();

    DocumentModel confusableWithUppercase = createDocument(session,
        session.createDocumentModel(path, "ā", FV_CHARACTER));
    confusableWithUppercase.setPropertyValue("dc:title", "ā");
    confusableWithUppercase.setPropertyValue("fvcharacter:upper_case_character", "a‾");
    session.saveDocument(confusableWithUppercase);

    DocumentModel addedConfusables = session.saveDocument(addConfusablesService
        .updateConfusableCharacters(session, confusableWithUppercase, dialect, "ā",
            confusablesToAdd));

    List<String> confusableList = Arrays
        .asList((String[]) addedConfusables.getPropertyValue("fvcharacter:confusable_characters"));

    //Assert.assertEquals(5, confusableList.size());
    Assert.assertFalse(confusableList.contains("ā"));
    Assert.assertFalse(confusableList.contains("a‾"));

  }

}