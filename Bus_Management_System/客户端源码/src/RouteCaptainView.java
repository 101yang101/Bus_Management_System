import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.CallableStatement;

public class RouteCaptainView implements AutoCloseable{
    private final Connection connection;
    private int companyID;
    private String workNum;

    // 构造函数接收一个已有的数据库连接，并进行登录验证
    public RouteCaptainView(int companyID, String workNum) throws SQLException, DriverView.LoginFailedException {
        this.connection = DatabaseConnection.connect();
        this.companyID = companyID;
        this.workNum = workNum;

        if (!login()) {
            throw new DriverView.LoginFailedException("登录失败: 未找到该路队长的信息");
        }
    }

    // 执行登录验证
    private boolean login() throws SQLException {
        final String QUERY_VERIFY_DRIVER = "SELECT 1 FROM Driver_View WHERE company_ID = ? AND driver_work_num = ? AND driver_job = '路队长' FETCH FIRST 1 ROW ONLY";


        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_VERIFY_DRIVER)) {
            pstmt.setInt(1, companyID);
            pstmt.setString(2, workNum);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // 如果查询结果存在，则表示登录成功
            }
        }
    }

    // 查询路队长的基本信息
    public void queryRouteCaptainInfo(int companyID, String workNum) throws SQLException {
        final String QUERY_DRIVER_INFO =
                "SELECT driver_name, company_name, driver_work_num, driver_ID, driver_job, driver_birthday, driver_phone, driver_route_ID, driver_fleet_ID, driver_gender, car_ID " +
                        "FROM Driver_View WHERE company_ID = ? AND driver_work_num = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_DRIVER_INFO)) {
            pstmt.setInt(1, companyID);
            pstmt.setString(2, workNum);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("司机基本信息:");
                    System.out.println("姓名: " + rs.getString("driver_name"));
                    System.out.println("公司名称: " + rs.getString("company_name"));
                    System.out.println("工作编号: " + rs.getString("driver_work_num"));
                    System.out.println("身份证号码: " + rs.getString("driver_ID"));
                    System.out.println("职位: " + rs.getString("driver_job"));
                    System.out.println("出生日期: " + rs.getDate("driver_birthday"));
                    System.out.println("电话号码: " + rs.getString("driver_phone"));
                    System.out.println("线路: " + rs.getInt("driver_route_ID"));
                    System.out.println("车队编号: " + rs.getInt("driver_fleet_ID"));
                    System.out.println("性别: " + rs.getString("driver_gender"));
                    System.out.println("车牌号: " + rs.getString("car_ID"));
                } else {
                    System.out.println("未找到该路队长的信息");
                }
            }
        }
    }

    // 查询自己在某个时间段的违章详细信息
    public List<DriverView.ViolationDetail> queryViolationDetails(int companyID, String workNum, LocalDateTime startTime, LocalDateTime endTime) throws SQLException {
        final String QUERY_VIOLATION_DETAILS =
                "SELECT dv.driver_name, dv.violation_name, dv.violation_punishment, dv.record_time, dv.record_station_ID, dv.record_car_ID " +
                        "FROM Driver_Violation_View dv " +
                        "WHERE dv.company_ID = ? AND dv.driver_work_num = ? AND dv.record_time BETWEEN ? AND ?";

        List<DriverView.ViolationDetail> violations = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_VIOLATION_DETAILS)) {
            pstmt.setInt(1, companyID);
            pstmt.setString(2, workNum);
            pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(startTime));
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(endTime));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    violations.add(new DriverView.ViolationDetail(
                            rs.getString("driver_name"),
                            rs.getString("violation_name"),
                            rs.getString("violation_punishment"),
                            rs.getTimestamp("record_time").toLocalDateTime(),
                            rs.getInt("record_station_ID"),
                            rs.getString("record_car_ID")
                    ));
                }
            }
        }

        if (violations.isEmpty()) {
            System.out.println("该司机在此时间段内无违章记录");
        }

        return violations;
    }

    // 展示由自己管理的司机的信息
    public void displayManagedDriversInTable() {
        String functionCall = "SELECT * FROM get_captain_drivers(?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(functionCall)) {
            stmt.setInt(1, this.companyID);
            stmt.setString(2, this.workNum);

            try (ResultSet rs = stmt.executeQuery()) {
                printTableHeader(); // 打印表头
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    printTableRow(
                            rs.getString("driver_name"),
                            rs.getString("driver_work_num"),
                            rs.getString("driver_ID"),
                            rs.getString("car_ID"),
                            rs.getString("driver_phone")
                    );
                }
                if (!hasResults) {
                    System.out.println("没有找到相关记录。");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void printTableHeader() {
        System.out.printf("%-20s | %-20s | %-20s | %-20s | %-20s%n",
                "司机姓名", "工号", "身份证号码", "车牌号", "联系方式");
        printSeparatorLine();
    }

    private void printSeparatorLine() {
        System.out.println("--------------------------------------------------------------" +
                "-------------------------------------------------------------");
    }

    private void printTableRow(String driverName, String driverWorkNum, String driverID, String carID, String driverPhone) {
        System.out.printf("%-20s | %-20s | %-20s | %-20s | %-20s%n",
                driverName, driverWorkNum, driverID, carID, driverPhone);
    }


    // 判断给定的司机是否由当前路队长管理
    public boolean isDriverManagedByRouteCaptain(int companyID, String workNum) {
        String sql = "SELECT driver_fleet_ID, driver_route_ID FROM Driver "
                + "WHERE driver_fleet_company_ID = ? AND driver_work_num = ? ";

        int route_captain_fleet_ID = 0;
        int route_captain_route_ID = 0;

        int driver_fleet_ID = 0;
        int driver_route_ID = 0;

        // SQL查询语句：检查路队长的车队ID、线路ID
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, this.companyID);
            stmt.setString(2, this.workNum);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    route_captain_fleet_ID = rs.getInt("driver_fleet_ID");
                    route_captain_route_ID = rs.getInt("driver_route_ID");
                }
            }
        } catch (SQLException e) {
            System.err.println("查询失败: " + e.getMessage());
        }

        // SQL查询语句：检查司机的车队ID、线路ID
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, companyID);
            stmt.setString(2, workNum);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    driver_fleet_ID = rs.getInt("driver_fleet_ID");
                    driver_route_ID = rs.getInt("driver_route_ID");
                }
            }
        } catch (SQLException e) {
            System.err.println("查询失败: " + e.getMessage());
        }

        if(companyID == this.companyID && driver_fleet_ID == route_captain_fleet_ID && driver_route_ID == route_captain_route_ID)
        {
            return true;
        }
        else{
            return false;
        }
    }

    // 判断给定的司机是否由当前路队长管理
    public boolean isDriverManagedByRouteCaptain(String driverID) {
        String sql = "SELECT driver_fleet_ID, driver_route_ID FROM Driver "
                + "WHERE driver_fleet_company_ID = ? AND driver_work_num = ? ";

        int route_captain_fleet_ID = 0;
        int route_captain_route_ID = 0;

        int driver_company_ID = 0;
        int driver_fleet_ID = 0;
        int driver_route_ID = 0;

        // SQL查询语句：检查路队长的车队ID、线路ID
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, this.companyID);
            stmt.setString(2, this.workNum);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    route_captain_fleet_ID = rs.getInt("driver_fleet_ID");
                    route_captain_route_ID = rs.getInt("driver_route_ID");
                }
            }
        } catch (SQLException e) {
            System.err.println("查询失败: " + e.getMessage());
        }

        sql = "SELECT driver_fleet_company_ID, driver_fleet_ID, driver_route_ID FROM Driver "
                + "WHERE driver_ID = ?";

        // SQL查询语句：检查司机的车队ID、线路ID
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, driverID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    driver_company_ID = rs.getInt("driver_fleet_company_ID");
                    driver_fleet_ID = rs.getInt("driver_fleet_ID");
                    driver_route_ID = rs.getInt("driver_route_ID");
                }
            }
        } catch (SQLException e) {
            System.err.println("查询失败: " + e.getMessage());
        }

        if(driver_company_ID == this.companyID && driver_fleet_ID == route_captain_fleet_ID && driver_route_ID == route_captain_route_ID)
        {
            return true;
        }
        else{
            return false;
        }
    }

    // 查询某司机所属车队在某个时间段的违章统计信息，并直接打印结果
    public void queryFleetViolationStatsForRouteCaptain(LocalDateTime startTime, LocalDateTime endTime) {
        // 第一步：根据(company_ID, workNum)查询司机所属的fleet_ID
        int fleetID = -1;
        try {
            fleetID = getDriverFleetID();

            if (fleetID == -1) {
                System.out.println("未找到该司机的信息或司机不属于任何车队");
                return;
            }

            // 第二步：查询该车队在指定时间段的违章统计信息并打印
            printFleetViolationStats(fleetID, startTime, endTime);
        } catch (SQLException e) {
            System.err.println("查询过程中发生错误: " + e.getMessage());
        }
    }

    // 获取司机所属的fleet_ID
    private int getDriverFleetID() throws SQLException {
        final String QUERY_DRIVER_FLEET_ID =
                "SELECT driver_fleet_ID " +
                        "FROM Driver " +
                        "WHERE driver_fleet_company_ID = ? AND driver_work_num = ? " +
                        "LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_DRIVER_FLEET_ID)) {
            pstmt.setInt(1, this.companyID);
            pstmt.setString(2, this.workNum);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("driver_fleet_ID");
                } else {
                    return -1; // 表示未找到对应的车队ID
                }
            }
        }
    }

    // 打印某车队在某个时间段的违章统计信息
    private void printFleetViolationStats(int fleetID, LocalDateTime startTime, LocalDateTime endTime) throws SQLException {
        final String QUERY_FLEET_VIOLATION_STATS =
                "SELECT dv.violation_name, COUNT(*) AS violation_count " +
                        "FROM Driver_Violation_View dv " +
                        "WHERE dv.driver_fleet_ID = ? AND dv.record_time BETWEEN ? AND ? " +
                        "GROUP BY dv.violation_name";

        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_FLEET_VIOLATION_STATS)) {
            pstmt.setInt(1, fleetID);
            pstmt.setTimestamp(2, java.sql.Timestamp.valueOf(startTime));
            pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(endTime));

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasViolations = false;
                while (rs.next()) {
                    hasViolations = true;
                    String violationName = rs.getString("violation_name");
                    int count = rs.getInt("violation_count");
                    System.out.println(violationName + ": " + count + "次");
                }
                if (!hasViolations) {
                    System.out.println("该车队在此时间段内无违章记录");
                }
            }
        }
    }

    // 录入汽车信息
    public boolean addCar(String carID, int seatNum) {
        String sql = "INSERT INTO Car (car_ID, car_company_ID, car_seat_num) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, carID);
            stmt.setInt(2, companyID); // 关联到当前路队长的公司
            stmt.setInt(3, seatNum);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("录入汽车信息失败: " + e.getMessage());
        }
        return false;
    }

    // 录入司机信息
    public boolean addDriver(String workNum, String name, char gender, String id, String phone, Date birthday, String carID) {
        String sql1 = "SELECT driver_fleet_ID, driver_route_ID FROM Driver "
                + "WHERE driver_fleet_company_ID = ? AND driver_work_num = ? ";

        int route_captain_fleet_ID = 0;
        int route_captain_route_ID = 0;

        // SQL查询语句：检查路队长的车队ID、线路ID
        try (PreparedStatement stmt = connection.prepareStatement(sql1)) {
            stmt.setInt(1, this.companyID);
            stmt.setString(2, this.workNum);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    route_captain_fleet_ID = rs.getInt("driver_fleet_ID");
                    route_captain_route_ID = rs.getInt("driver_route_ID");
                }
            }
        } catch (SQLException e) {
            System.err.println("查询失败: " + e.getMessage());
        }

        String sql2 = "INSERT INTO Driver (driver_name, driver_fleet_company_ID, driver_fleet_ID, driver_ID, driver_work_num, driver_job, driver_birthday, driver_phone, driver_gender, driver_route_ID, driver_car_ID) "
                + "VALUES (?, ?, ?, ?, ?, '司机', ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql2)) {
            stmt.setString(1, name);
            stmt.setInt(2, companyID); // 关联到当前路队长的公司
            stmt.setInt(3, route_captain_fleet_ID);
            stmt.setString(4, id);
            stmt.setString(5, workNum);
            stmt.setDate(6, birthday);
            stmt.setString(7, phone);
            stmt.setString(8, Character.toString(gender));
            stmt.setInt(9, route_captain_route_ID);
            stmt.setString(10, carID);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("录入司机信息失败: " + e.getMessage());
        }
        return false;
    }

    // 录入违章信息
    public boolean recordViolation(int violationID, int stationID, String driverID, String carID, LocalDateTime violationTime, String recorderID) {
        String sql = "INSERT INTO Violation_Record (record_violation_ID, record_station_ID, record_driver_ID, record_car_ID, record_time, record_recorder_ID) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, violationID);
            stmt.setInt(2, stationID);
            stmt.setString(3, driverID);
            stmt.setString(4, carID);
            stmt.setTimestamp(5, Timestamp.valueOf(violationTime));
            stmt.setString(6, recorderID);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("记录违章信息失败: " + e.getMessage());
        }
        return false;
    }

    // 内部类：用于封装违章详细信息
    public static class ViolationDetail {
        private final String driverName;
        private final String violationName;
        private final String violationPunishment;
        private final LocalDateTime recordTime;
        private final int recordStationID;
        private final String recordCarID;

        public ViolationDetail(String driverName, String violationName, String violationPunishment,
                               LocalDateTime recordTime, int recordStationID, String recordCarID) {
            this.driverName = driverName;
            this.violationName = violationName;
            this.violationPunishment = violationPunishment;
            this.recordTime = recordTime;
            this.recordStationID = recordStationID;
            this.recordCarID = recordCarID;
        }

        @Override
        public String toString() {
            return "违章详细信息:\n" +
                    "司机姓名: " + driverName + "\n" +
                    "违规名称: " + violationName + "\n" +
                    "处罚措施: " + violationPunishment + "\n" +
                    "违规时间: " + recordTime + "\n" +
                    "违规地点（站点ID）: " + recordStationID + "\n" +
                    "违规车辆（车牌号）: " + recordCarID + "\n" +
                    "-----------------------------";
        }
    }

    // 定义自定义异常类以处理登录失败的情况
    public static class LoginFailedException extends Exception {
        public LoginFailedException(String message) {
            super(message);
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}