module Common {

    requires sqlite.jdbc;
    requires java.sql;

    opens com.chatApplication.dataModel;
    opens com.chatApplication.common;

    exports com.chatApplication.dataModel;
    exports com.chatApplication.common;
}