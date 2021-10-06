package org.example;

import java.sql.SQLException;

public class StartBoot {
    public static void main(String[] args) throws SQLException {
        HexoToTypecho.readFileToString("D:\\web\\hexo\\blog\\myblog\\source\\_posts");
    }
}
