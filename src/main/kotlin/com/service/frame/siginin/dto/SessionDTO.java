package com.service.frame.siginin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDTO {
    private static final long serialVersionUID = -5624457780444463151L;

    @Id
    private String session_usrid;
    private String session_usrname;
    private String session_orgid;
    private String session_orgname;
    private String session_instcd;
    private String session_instname;

    private String session_message;
    @Override
    public int hashCode() {
        return Objects.hash(session_instcd, session_instname, session_message, session_orgid, session_orgname,
                session_usrid, session_usrname);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SessionDTO other = (SessionDTO) obj;
        return Objects.equals(session_instcd, other.session_instcd)
                && Objects.equals(session_instname, other.session_instname)
                && Objects.equals(session_message, other.session_message)
                && Objects.equals(session_orgid, other.session_orgid)
                && Objects.equals(session_orgname, other.session_orgname)
                && Objects.equals(session_usrid, other.session_usrid)
                && Objects.equals(session_usrname, other.session_usrname);
    }

}
