package ir.maktab127.config;

import java.util.Collection;
import java.util.Collections;

public interface HttpRequestsCustomizer {

    default String[] permitAll() {
        return new String[0];
    }

    default Collection<AuthorityBased> authorizeByAuthority() {
        return Collections.emptyList();
    }


    interface AuthorityBased {
        default String[] urls() {
            return new String[0];
        }

        default String[] authorities() {
            return new String[0];
        }
    }


}
