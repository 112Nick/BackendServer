package project.model;

import java.math.BigDecimal;

public interface User {

    BigDecimal getId();

    String getLogin();

    String getEmail();

    String getDefault_email();

    String getToken();

    void setId(BigDecimal id);

}
