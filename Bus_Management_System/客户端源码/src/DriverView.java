import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DriverView implements AutoCloseable{
    private final Connection connection;
    private int companyID;
    private String workNum;

    // 构造函数接收一个已有的数据库连接，并进行登录验证
    public DriverView(int companyID, String workNum) throws SQLException, LoginFailedException {
        this.connection = DatabaseConnection.connect();
        this.companyID = companyID;
        this.workNum = workNum;

        if (!login()) {
            throw new LoginFailedException("登录失败: 未找到该司机的信息");
        }
    }

    // 执行登录验证
    private boolean login() throws SQLException {
        final String QUERY_VERIFY_DRIVER = "SELECT 1 FROM Driver_View WHERE company_ID = ? AND driver_work_num = ? AND driver_job = '司机' FETCH FIRST 1 ROW ONLY";


        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_VERIFY_DRIVER)) {
            pstmt.setInt(1, companyID);
            pstmt.setString(2, workNum);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // 如果查询结果存在，则表示登录成功
            }
        }
    }

    // 查询司机的基本信息
    public void queryDriverInfo() throws SQLException {
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


    // 查询某名司机在某个时间段的违章详细信息
    public List<ViolationDetail> queryViolationDetails(LocalDateTime startTime, LocalDateTime endTime) throws SQLException {
        final String QUERY_VIOLATION_DETAILS =
                "SELECT dv.driver_name, dv.violation_name, dv.violation_punishment, dv.record_time, dv.record_station_ID, dv.record_car_ID " +
                        "FROM Driver_Violation_View dv " +
                        "WHERE dv.company_ID = ? AND dv.driver_work_num = ? AND dv.record_time BETWEEN ? AND ?";

        List<ViolationDetail> violations = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_VIOLATION_DETAILS)) {
            pstmt.setInt(1, this.companyID);
            pstmt.setString(2, this.workNum);
            pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(startTime));
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(endTime));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    violations.add(new ViolationDetail(
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


    // 查询某司机所属车队在某个时间段的违章统计信息，并直接打印结果
    public void queryFleetViolationStatsForDriver(LocalDateTime startTime, LocalDateTime endTime) {
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