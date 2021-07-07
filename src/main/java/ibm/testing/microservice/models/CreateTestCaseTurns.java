/* ***************************************************************** */
/*                                                                   */
/* Licensed Materials - Property of IBM                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2019. All Rights Reserved.                */
/*                                                                   */
/* US Government Users Restricted Rights - Use, duplication or       */
/* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. */
/*                                                                   */
/* ***************************************************************** */
package ibm.testing.microservice.models;

import java.util.Objects;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.Valid;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * CreateTestCaseTurns
 */
public class CreateTestCaseTurns   {
	@JsonbProperty("receive")
  private ReceiveType receive = null;

	@JsonbProperty("send")
  private SendType send = null;

  public CreateTestCaseTurns receive(ReceiveType receive) {
    this.receive = receive;
    return this;
  }

  /**
   * Get receive
   * @return receive
   **/
  @JsonbProperty("receive")
  @Schema(description="")
  @Valid
  public ReceiveType getReceive() {
    return receive;
  }

  public void setReceive(ReceiveType receive) {
    this.receive = receive;
  }

  public CreateTestCaseTurns send(SendType send) {
    this.send = send;
    return this;
  }

  /**
   * Get send
   * @return send
   **/
  @JsonbProperty("send")
  @Schema(description="")
  @Valid
  public SendType getSend() {
    return send;
  }

  public void setSend(SendType send) {
    this.send = send;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateTestCaseTurns createTestCaseTurns = (CreateTestCaseTurns) o;
    return Objects.equals(this.receive, createTestCaseTurns.receive) &&
        Objects.equals(this.send, createTestCaseTurns.send);
  }

  @Override
  public int hashCode() {
    return Objects.hash(receive, send);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TestCaseTurns {\n");
    
    sb.append("    receive: ").append(toIndentedString(receive)).append("\n");
    sb.append("    send: ").append(toIndentedString(send)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
