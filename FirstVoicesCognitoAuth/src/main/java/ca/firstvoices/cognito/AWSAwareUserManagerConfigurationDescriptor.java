package ca.firstvoices.cognito;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("configuration")
public class AWSAwareUserManagerConfigurationDescriptor {

  @XNode("authenticateWithCognito") public boolean authenticateWithCognito;

  @XNode("useCognitoAsPrincipalDirectory") public boolean useCognitoAsPrincipalDirectory;

  @XNode("ignoreUsers") public String ignoreUsers;

  @Override
  public String toString() {
    return "AWSAwareUserManagerConfigurationDescriptor{" + "authenticateWithCognito="
        + authenticateWithCognito + ", useCognitoAsPrincipalDirectory="
        + useCognitoAsPrincipalDirectory + ", ignoreUsers=" + ignoreUsers + '}';
  }
}
