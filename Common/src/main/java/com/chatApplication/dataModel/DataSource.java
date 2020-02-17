package com.chatApplication.dataModel;

import com.chatApplication.common.NewUser;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DataSource {

    /*public static final String DB_NAME = "database" + File.separator + "ChatUsersDB.db";
    public static final String PATH = Paths.get(".").toAbsolutePath().normalize().toString() +
            File.separator +"src" + File.separator +"main" + File.separator +"resources" + File.separator;
    public static final String CONNECTION_STRING = "jdbc:sqlite:" + PATH + DB_NAME;*/

    public static final String DB_NAME = "ChatUsersDB.db";
    public static final String PATH = System.getProperty("user.home") + File.separator + "Documents" + File.separator +
            "MessengerApplication" + File.separator + "Database" + File.separator;
    public static final String CONNECTION_STRING = "jdbc:sqlite:" + PATH + DB_NAME;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_PICTURE = "picture";

    private static final String QUERY_USERS_CHECK_EXISTS = "SELECT * FROM " + TABLE_USERS + " WHERE " +
            COLUMN_USERNAME + " = ?";

    private static final String QUERY_LOGIN = "SELECT " + COLUMN_USERNAME + ", " + COLUMN_PASSWORD +
            " FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";

    private static final String INSERT_USER = "INSERT INTO " + TABLE_USERS + '(' + COLUMN_USERNAME + ", " + COLUMN_PASSWORD
            + ", " + COLUMN_PHONE + ", " + COLUMN_PICTURE + ") VALUES(?, ?, ?, ?)";

    private static final String QUERY_PICTURE = "SELECT " + COLUMN_PICTURE + " FROM " + TABLE_USERS + " WHERE "
            + COLUMN_USERNAME + " = ?";

    private PreparedStatement queryUsersCheckExists;
    private PreparedStatement queryLogin;
    private PreparedStatement inserUser;
    private PreparedStatement queryPicture;

    private Connection connection;

    private static DataSource instance = new DataSource();

    private DataSource() {
    }

    public static DataSource getInstance() {
        return instance;
    }

    public boolean open() {
        try {
            connection = DriverManager.getConnection(CONNECTION_STRING);
            queryUsersCheckExists = connection.prepareStatement(QUERY_USERS_CHECK_EXISTS);
            queryLogin = connection.prepareStatement(QUERY_LOGIN);
            inserUser = connection.prepareStatement(INSERT_USER);
            queryPicture = connection.prepareStatement(QUERY_PICTURE);

            System.out.println("Successfully connected to the Database.");
            return true;
        } catch (SQLException e) {
            System.out.println("Couldn't connect to the Database.");
            System.out.println("SQL exception " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (queryUsersCheckExists != null) {
                queryUsersCheckExists.close();
            }
            if (queryLogin != null) {
                queryLogin.close();
            }
            if (inserUser != null) {
                inserUser.close();
            }
            if (queryPicture != null) {
                queryPicture.close();
            }
            if (connection != null) {
                connection.close();
            }

            System.out.println("Successfully disconnected from the Database.");
        } catch (SQLException e) {
            System.out.println("Couldn't close connection to the Database.");
            e.printStackTrace();
        }
    }

    private boolean checkUsernameExists(String username) {
        try {
            queryUsersCheckExists.setString(1, username);
            ResultSet results = queryUsersCheckExists.executeQuery();
            return results.next();
        } catch (SQLException e){
            System.out.println("Query failed: " + e.getMessage());
            return false;
        }
    }

    public Map<String,String> checkLogin(String username) {
        Map<String, String> resultMap = new HashMap<>();

        try {
            queryLogin.setString(1, username);
            ResultSet results = queryLogin.executeQuery();

            String dbUsername = "";
            String dbPassword = "";

            if (results.next()) {
                dbUsername = results.getString(1);
                dbPassword = results.getString(2);
            }

            if (!(dbUsername.equals("") && dbPassword.equals(""))) {
                resultMap.put("username", dbUsername);
                resultMap.put("password", dbPassword);

                return resultMap;
            } else {
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public String insertNewUser(NewUser user) {
        if (!checkUsernameExists(user.getUsername())) {
            try {
                inserUser.setString(1, user.getUsername());
                inserUser.setString(2, user.getPassword());
                inserUser.setString(3, user.getPhone());
                if (!user.getPicture().equals("")) {
                    inserUser.setBytes(4, readFile(user.getPicture()));
                } else {
                    File file = new File(getClass().getResource("/images/user.png").getFile());
                    String path = file.getAbsolutePath();
                    inserUser.setBytes(4, readFile(path));
                }

                System.out.println("\nStoring user in database: " + user.toString());
                inserUser.executeUpdate();
                System.out.println("\nCompleted successfully!");

                return "New Account Created";

            } catch (SQLException e) {
                e.printStackTrace();
                return "Database error";
            }
        }
        return "Username exists";
    }

    public String queryUserPicture(String username) {
        try {
            String path = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "MessengerApplication" + File.separator + username + ".jpg";
            queryPicture.setString(1, username);
            ResultSet results = queryPicture.executeQuery();

            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);

            while (results.next()) {
                InputStream input = results.getBinaryStream(1);
                byte[] buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    fos.write(buffer);
                }
            }

            return file.getAbsolutePath();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    private byte[] readFile(String path) {
        ByteArrayOutputStream bos = null;
        try {
            File file = new File(path);
            FileInputStream input = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            System.out.println("Reading input file: " + file.getAbsolutePath());
            for (int len; (len = input.read(buffer)) != -1;){
                bos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos != null ? bos.toByteArray() : null;
    }

    public static void main(String[] args) throws IOException, SQLException, NoSuchAlgorithmException {
        DataSource.getInstance().open();

        DataSource.getInstance().close();
    }
}