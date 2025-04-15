import java.time.LocalDateTime;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.sql.Date;
import java.time.format.DateTimeFormatter;

public class Main {
    private static void Call_DriverView(int companyId, String driverWorkNum)
    {
        // 创建 DriverView 实例
        try (DriverView dv = new DriverView(companyId, driverWorkNum)) {
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                System.out.println("\n请选择要执行的操作：");
                System.out.println("1. 查询司机基本信息");
                System.out.println("2. 查询违章详细信息");
                System.out.println("3. 查询车队违章统计");
                System.out.println("4. 退出");
                System.out.print("选择: ");

                String choice = scanner.nextLine();
                switch (choice) {
                    case "1":
                        dv.queryDriverInfo();
                        break;
                    case "2":
                        System.out.print("请输入开始时间（格式：yyyy-MM-dd HH:mm）: ");
                        LocalDateTime startTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        System.out.print("请输入结束时间（格式：yyyy-MM-dd HH:mm）: ");
                        LocalDateTime endTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        List<DriverView.ViolationDetail> details = dv.queryViolationDetails(startTime, endTime);
                        for (DriverView.ViolationDetail detail : details) {
                            System.out.println(detail);
                        }
                        break;
                    case "3":
                        System.out.print("请输入开始时间（格式：yyyy-MM-dd HH:mm）: ");
                        startTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        System.out.print("请输入结束时间（格式：yyyy-MM-dd HH:mm）: ");
                        endTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        dv.queryFleetViolationStatsForDriver(startTime, endTime);
                        break;
                    case "4":
                        exit = true;
                        break;
                    default:
                        System.out.println("无效的选择，请重新输入。");
                }
            }
        } catch (SQLException e) {
            System.err.println("数据库操作失败: " + e.getMessage());
        } catch (DriverView.LoginFailedException e) {
            System.err.println("登录失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("其他错误: " + e.getMessage());
        }
    }


    private static void Call_RouteCaptainView(int companyId, String workNum) {
        // 创建 RouteCaptainView 实例
        try (RouteCaptainView rcv = new RouteCaptainView(companyId, workNum)) {
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                System.out.println("\n请选择要执行的操作：");
                System.out.println("1. 查询路队长基本信息");
                System.out.println("2. 查询由自己管理的司机信息");
                System.out.println("3. 查询自己的违章详细信息");
                System.out.println("4. 查询由自己管理的司机的违章详细信息");
                System.out.println("5. 插入汽车信息");
                System.out.println("6. 插入司机信息");
                System.out.println("7. 查询车队违章统计情况");
                System.out.println("8. 记录违章信息");
                System.out.println("9. 退出");
                System.out.print("选择: ");

                String choice = scanner.nextLine();
                switch (choice) {
                    case "1":
                        rcv.queryRouteCaptainInfo(companyId, workNum);
                        break;
                    case "2":
                        rcv.displayManagedDriversInTable();
                        break;
                    case "3":
                        System.out.print("请输入开始时间（格式：yyyy-MM-dd HH:mm）: ");
                        LocalDateTime startTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        System.out.print("请输入结束时间（格式：yyyy-MM-dd HH:mm）: ");
                        LocalDateTime endTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        List<DriverView.ViolationDetail> details = rcv.queryViolationDetails(companyId, workNum, startTime, endTime);
                        for (DriverView.ViolationDetail detail : details) {
                            System.out.println(detail);
                        }
                        break;
                    case "4":
                        System.out.print("请输入司机工号: ");
                        String driverWorkNum = scanner.nextLine();
                        System.out.print("请输入开始时间（格式：yyyy-MM-dd HH:mm）: ");
                        startTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        System.out.print("请输入结束时间（格式：yyyy-MM-dd HH:mm）: ");
                        endTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        details = rcv.queryViolationDetails(companyId, driverWorkNum, startTime, endTime);
                        for (DriverView.ViolationDetail detail : details) {
                            System.out.println(detail);
                        }
                        break;
                    case "5":
                        System.out.print("请输入车牌号: ");
                        String carID = scanner.nextLine();
                        System.out.print("请输入座位数: ");
                        int seatNum = Integer.parseInt(scanner.nextLine());
                        if (rcv.addCar(carID, seatNum)) {
                            System.out.println("汽车信息录入成功！");
                        } else {
                            System.out.println("汽车信息录入失败！");
                        }
                        break;
                    case "6":
                        System.out.print("请输入司机工号: ");
                        driverWorkNum = scanner.nextLine();
                        System.out.print("请输入司机姓名: ");
                        String name = scanner.nextLine();
                        System.out.print("请输入司机性别（M/F）: ");
                        char gender = scanner.nextLine().charAt(0);
                        System.out.print("请输入司机身份证号码: ");
                        String id = scanner.nextLine();
                        System.out.print("请输入司机电话号码: ");
                        String phone = scanner.nextLine();
                        System.out.print("请输入司机出生日期（格式：yyyy-MM-dd）: ");
                        Date birthday = Date.valueOf(scanner.nextLine());
                        System.out.print("请输入所属车辆车牌号（可选）: ");
                        String carIDForDriver = scanner.nextLine();
                        if (rcv.addDriver(driverWorkNum, name, gender, id, phone, birthday, carIDForDriver)) {
                            System.out.println("司机信息录入成功！");
                        } else {
                            System.out.println("司机信息录入失败！");
                        }
                        break;
                    case "7":
                        System.out.print("请输入开始时间（格式：yyyy-MM-dd HH:mm）: ");
                        startTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        System.out.print("请输入结束时间（格式：yyyy-MM-dd HH:mm）: ");
                        endTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        rcv.queryFleetViolationStatsForRouteCaptain(startTime, endTime);
                        break;
                    case "8":
                        System.out.println("准备记录违章信息，请输入以下详细信息：");

                        // 获取违规类型ID
                        System.out.print("请输入违规类型ID: ");
                        int violationID;
                        violationID = Integer.parseInt(scanner.nextLine());
                        // 获取发生违规的站点ID
                        System.out.print("请输入发生违规的站点ID: ");
                        int stationID = Integer.parseInt(scanner.nextLine());
                        // 获取司机身份证号码
                        System.out.print("请输入司机身份证号码: ");
                        String driverID = scanner.nextLine();
                        // 获取车牌号
                        System.out.print("请输入车牌号: ");
                        carID = scanner.nextLine();
                        // 获取违规时间
                        LocalDateTime violationTime;
                        System.out.print("请输入违规时间（格式：yyyy-MM-dd HH:mm:ss）: ");
                        violationTime = LocalDateTime.parse(scanner.nextLine(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        // 获取记录人身份证号码
                        System.out.print("请输入记录人身份证号码: ");
                        String recorderID = scanner.nextLine();

                        if(rcv.isDriverManagedByRouteCaptain(driverID))
                        {
                            // 尝试记录违章信息
                            if (rcv.recordViolation(violationID, stationID, driverID, carID, violationTime, recorderID)) {
                                System.out.println("违章信息记录成功！");
                            } else {
                                System.out.println("违章信息记录失败！");
                            }
                        }
                        else
                        {
                            System.out.println("您无权记录！");
                        }
                        break;
                    case "9":
                        exit = true;
                        break;
                    default:
                        System.out.println("无效的选择，请重新输入。");
                }
            }
        } catch (SQLException e) {
            System.err.println("数据库操作失败: " + e.getMessage());
        } catch (DriverView.LoginFailedException e) {
            System.err.println("登录失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("其他错误: " + e.getMessage());
        }
    }




    private static void Call_FleetCaptainView(int companyId, String workNum) {
        // 创建 FleetCaptainView 实例
        try (FleetCaptainView rcv = new FleetCaptainView(companyId, workNum)) {
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                System.out.println("\n请选择要执行的操作：");
                System.out.println("1. 查询路队长基本信息");
                System.out.println("2. 查询由自己管理的司机信息");
                System.out.println("3. 查询由自己管理的司机的违章详细信息");
                System.out.println("4. 插入汽车信息");
                System.out.println("5. 插入司机信息");
                System.out.println("6. 查询车队违章统计情况");
                System.out.println("7. 记录违章信息");
                System.out.println("8. 退出");
                System.out.print("选择: ");

                String choice = scanner.nextLine();
                switch (choice) {
                    case "1":
                        rcv.queryRouteCaptainInfo(companyId, workNum);
                        break;
                    case "2":
                        rcv.displayManagedDriversInTable();
                        break;
                    case "3":
                        System.out.print("请输入司机工号: ");
                        String driverWorkNum = scanner.nextLine();
                        System.out.print("请输入开始时间（格式：yyyy-MM-dd HH:mm）: ");
                        LocalDateTime startTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        System.out.print("请输入结束时间（格式：yyyy-MM-dd HH:mm）: ");
                        LocalDateTime endTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        List<DriverView.ViolationDetail> details = rcv.queryViolationDetails(companyId, driverWorkNum, startTime, endTime);
                        for (DriverView.ViolationDetail detail : details) {
                            System.out.println(detail);
                        }
                        break;
                    case "4":
                        System.out.print("请输入车牌号: ");
                        String carID = scanner.nextLine();
                        System.out.print("请输入座位数: ");
                        int seatNum = Integer.parseInt(scanner.nextLine());
                        if (rcv.addCar(carID, seatNum)) {
                            System.out.println("汽车信息录入成功！");
                        } else {
                            System.out.println("汽车信息录入失败！");
                        }
                        break;
                    case "5":
                        System.out.print("请输入司机工号: ");
                        driverWorkNum = scanner.nextLine();
                        System.out.print("请输入司机姓名: ");
                        String name = scanner.nextLine();
                        System.out.print("请输入司机性别（M/F）: ");
                        char gender = scanner.nextLine().charAt(0);
                        System.out.print("请输入司机身份证号码: ");
                        String id = scanner.nextLine();
                        System.out.print("请输入司机电话号码: ");
                        String phone = scanner.nextLine();
                        System.out.print("请输入司机出生日期（格式：yyyy-MM-dd）: ");
                        Date birthday = Date.valueOf(scanner.nextLine());
                        System.out.print("请输入所属车辆车牌号（可选）: ");
                        String carIDForDriver = scanner.nextLine();
                        System.out.print("请输入司机的路线: ");
                        int driver_route_ID = Integer.parseInt(scanner.nextLine());
                        System.out.print("请输入司机的职位: ");
                        String job = scanner.nextLine();
                        if (rcv.addDriver(driverWorkNum, name, gender, id, phone, birthday, carIDForDriver, driver_route_ID, job)) {
                            System.out.println("司机信息录入成功！");
                        } else {
                            System.out.println("司机信息录入失败！");
                        }
                        break;
                    case "6":
                        System.out.print("请输入开始时间（格式：yyyy-MM-dd HH:mm）: ");
                        startTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        System.out.print("请输入结束时间（格式：yyyy-MM-dd HH:mm）: ");
                        endTime = LocalDateTime.parse(scanner.nextLine(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        rcv.queryFleetViolationStatsForRouteCaptain(startTime, endTime);
                        break;
                    case "7":
                        System.out.println("准备记录违章信息，请输入以下详细信息：");

                        // 获取违规类型ID
                        System.out.print("请输入违规类型ID: ");
                        int violationID;
                        violationID = Integer.parseInt(scanner.nextLine());
                        // 获取发生违规的站点ID
                        System.out.print("请输入发生违规的站点ID: ");
                        int stationID = Integer.parseInt(scanner.nextLine());
                        // 获取司机身份证号码
                        System.out.print("请输入司机身份证号码: ");
                        String driverID = scanner.nextLine();
                        // 获取车牌号
                        System.out.print("请输入车牌号: ");
                        carID = scanner.nextLine();
                        // 获取违规时间
                        LocalDateTime violationTime;
                        System.out.print("请输入违规时间（格式：yyyy-MM-dd HH:mm:ss）: ");
                        violationTime = LocalDateTime.parse(scanner.nextLine(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        // 获取记录人身份证号码
                        System.out.print("请输入记录人身份证号码: ");
                        String recorderID = scanner.nextLine();

                        if(rcv.isDriverManagedByRouteCaptain(driverID))
                        {
                            // 尝试记录违章信息
                            if (rcv.recordViolation(violationID, stationID, driverID, carID, violationTime, recorderID)) {
                                System.out.println("违章信息记录成功！");
                            } else {
                                System.out.println("违章信息记录失败！");
                            }
                        }
                        else
                        {
                            System.out.println("您无权记录！");
                        }
                        break;
                    case "8":
                        exit = true;
                        break;
                    default:
                        System.out.println("无效的选择，请重新输入。");
                }
            }
        } catch (SQLException e) {
            System.err.println("数据库操作失败: " + e.getMessage());
        } catch (DriverView.LoginFailedException e) {
            System.err.println("登录失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("其他错误: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        // 假设的公司ID和司机工作编号
        int companyId = 0; // 替换为实际的公司ID
        String workNum; // 替换为实际的司机工作编号

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("\n请选择您的身份：");
            System.out.println("1. 车队队长");
            System.out.println("2. 路队长");
            System.out.println("3. 司机");
            System.out.println("4. 退出");
            System.out.print("选择: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.print("请输入公司ID: ");
                    companyId = scanner.nextInt();
                    scanner.nextLine(); // 读取换行符，防止影响后续字符串输入
                    System.out.print("请输入工作编号: ");
                    workNum = scanner.nextLine();
                    Call_FleetCaptainView(companyId, workNum);
                    break;
                case "2":
                    System.out.print("请输入公司ID: ");
                    companyId = scanner.nextInt();
                    scanner.nextLine(); // 读取换行符，防止影响后续字符串输入
                    System.out.print("请输入工作编号: ");
                    workNum = scanner.nextLine();
                    Call_RouteCaptainView(companyId, workNum);
                    break;
                case "3":
                    System.out.print("请输入公司ID: ");
                    companyId = scanner.nextInt();
                    scanner.nextLine(); // 读取换行符，防止影响后续字符串输入
                    System.out.print("请输入工作编号: ");
                    workNum = scanner.nextLine();
                    Call_DriverView(companyId, workNum);
                    break;
                case "4":
                    exit = true;
                    break;
                default:
                    System.out.println("无效的选择，请重新输入。");
            }
        }
    }
}