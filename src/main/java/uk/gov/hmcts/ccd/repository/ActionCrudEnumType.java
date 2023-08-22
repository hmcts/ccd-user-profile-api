package uk.gov.hmcts.ccd.repository;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import uk.gov.hmcts.ccd.data.userprofile.AuditAction;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hibernate.type.SqlTypes.OTHER;

public class ActionCrudEnumType implements UserType<AuditAction> {

    @Override
    public int getSqlType() {
        return OTHER;
    }

    @Override
    public Class<AuditAction> returnedClass() {
        return AuditAction.class;
    }

    @Override
    public boolean equals(AuditAction x, AuditAction y) {
        return x.equals(y);
    }

    @Override
    public int hashCode(AuditAction x) {
        return x.hashCode();
    }

    @Override
    public AuditAction nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session,
                                   Object owner) throws SQLException {
        String string = rs.getString(position);
        return rs.wasNull() ? null : AuditAction.valueOf(string);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, AuditAction value, int index,
                            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, OTHER);
        } else {
            st.setString(index, value.toString());
        }
    }

    @Override
    public AuditAction deepCopy(AuditAction value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(AuditAction value) {
        return value;
    }

    @Override
    public AuditAction assemble(Serializable cached, Object owner) {
        return (AuditAction) cached;
    }
}
