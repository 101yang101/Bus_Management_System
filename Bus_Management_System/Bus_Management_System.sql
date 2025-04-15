/* 创建公司表 */
CREATE TABLE Company (
    company_ID SERIAL, -- SERIAL自动递增整数类型
    company_name VARCHAR(255) NOT NULL,
    company_establishment_date DATE NOT NULL, -- 公司成立日期
    PRIMARY KEY(company_ID)
);


/* 创建车队表 */
CREATE TABLE Fleet (
    fleet_ID SERIAL, -- 车队ID
    fleet_company_ID INT NOT NULL, -- 公司ID
    PRIMARY KEY (fleet_ID, fleet_company_ID) -- 组合主键
);


/* 创建汽车表 */
CREATE TABLE Car (
    car_ID VARCHAR(20) NOT NULL, -- 车牌号
    car_company_ID INT, -- 公司ID
    car_seat_num INT, -- 座位数量
    PRIMARY KEY (car_ID) -- 主键
);

/* 创建司机表 */
CREATE TABLE Driver (
    driver_name VARCHAR(255) NOT NULL, -- 司机姓名
    driver_fleet_company_ID INT NOT NULL, -- 司机所属车队所属公司ID
    driver_ID CHAR(18) NOT NULL UNIQUE, -- 身份证号码，作为唯一标识符
    driver_work_num VARCHAR(20) NOT NULL, -- 工作编号常嘉乐龙益辰田涵越孙榕润江国栋
    driver_job VARCHAR(255) NOT NULL, -- 职位（字符串）
    driver_birthday DATE, -- 出生日期
    driver_phone VARCHAR(15), -- 电话号码
    driver_gender CHAR(1), -- 性别，例如'M'表示男性，'F'表示女性
    driver_fleet_ID INT NOT NULL, -- 所属车队ID
    driver_car_ID VARCHAR(20), -- 所属车辆ID
    driver_route_ID INT,
    PRIMARY KEY (driver_fleet_company_ID, driver_work_num) -- 组合主键
);


/* 创建线路表 */
CREATE TABLE Route (
    route_ID SERIAL, -- 线路ID
    route_beginning_station_ID INT NOT NULL, -- 起始站ID
    route_ending_station_ID INT NOT NULL, -- 终点站ID
    PRIMARY KEY (route_ID) -- 主键
);

/* 创建站点表 */
CREATE TABLE Station (
    station_ID SERIAL, -- 站点ID
    station_name VARCHAR(255) NOT NULL, -- 站点名称
    PRIMARY KEY (station_ID) -- 主键
);


/* 创建条规表 */
CREATE TABLE Violation (
    violation_ID SERIAL, -- 使用SERIAL以实现自动递增的违规ID
    violation_name VARCHAR(255) NOT NULL, -- 违规名称
    violation_punishment TEXT, -- 违规惩罚
    PRIMARY KEY (violation_ID) -- 主键
);


/* 创建违规记录表 */
CREATE TABLE Violation_Record (
    record_ID SERIAL, -- 违规记录ID，自动递增
    record_violation_ID INT NOT NULL, -- 违规类型ID
    record_station_ID INT NOT NULL, -- 发生违规的站点ID
    record_driver_ID CHAR(18) NOT NULL, -- 司机身份证号码
    record_car_ID VARCHAR(20) NOT NULL, -- 车牌
    record_time TIMESTAMP NOT NULL, -- 违规时间（年月日时分秒）
    record_recorder_ID CHAR(18) NOT NULL, -- 记录人身份证
    PRIMARY KEY (record_ID) -- 显式定义主键
);

/* 创建车站-线路关系表 */
CREATE TABLE Route_Station (
    RS_station_ID INT NOT NULL, -- 站点ID
    RS_route_ID INT NOT NULL, -- 路线ID
    RS_sequence_in_route INT NOT NULL, -- 该站点在线路中的顺序
    PRIMARY KEY (RS_route_ID, RS_station_ID) -- 组合主键
);



/* 引入外码 */


-- 为Fleet表添加外键
ALTER TABLE Fleet
ADD CONSTRAINT fk_fleet_company_ID
FOREIGN KEY (fleet_company_ID) REFERENCES Company(company_ID);

-- 为Car表添加外键
ALTER TABLE Car
ADD CONSTRAINT fk_car_company
FOREIGN KEY (car_company_ID) REFERENCES Company(company_ID);

-- 为Driver表添加外键
ALTER TABLE Driver
ADD CONSTRAINT fk_driver_fleet
FOREIGN KEY (driver_fleet_company_ID, driver_fleet_ID) REFERENCES Fleet(fleet_company_ID, fleet_ID),
ADD CONSTRAINT fk_driver_car
FOREIGN KEY (driver_car_ID) REFERENCES Car(car_ID),
ADD CONSTRAINT fk_driver_route
FOREIGN KEY (driver_route_ID) REFERENCES Route(route_ID);

-- 为Route表添加外键
ALTER TABLE Route
ADD CONSTRAINT fk_route_beginning_station
FOREIGN KEY (route_beginning_station_ID) REFERENCES Station(station_ID),
ADD CONSTRAINT fk_route_ending_station
FOREIGN KEY (route_ending_station_ID) REFERENCES Station(station_ID);

-- 为Violation_Record表添加外键
ALTER TABLE Violation_Record
ADD CONSTRAINT fk_violation_record_violation
FOREIGN KEY (record_violation_ID) REFERENCES Violation(violation_ID),
ADD CONSTRAINT fk_violation_record_station
FOREIGN KEY (record_station_ID) REFERENCES Station(station_ID),
ADD CONSTRAINT fk_violation_record_driver
FOREIGN KEY (record_driver_ID) REFERENCES Driver(driver_ID),
ADD CONSTRAINT fk_violation_record_car
FOREIGN KEY (record_car_ID) REFERENCES Car(car_ID),
ADD CONSTRAINT fk_violation_record_recorder
FOREIGN KEY (record_recorder_ID) REFERENCES Driver(driver_ID); -- 记录人也是司机

-- 为Route_Station表添加外键
ALTER TABLE Route_Station
ADD CONSTRAINT fk_route_station_station
FOREIGN KEY (RS_station_ID) REFERENCES Station(station_ID),
ADD CONSTRAINT fk_route_station_route
FOREIGN KEY (RS_route_ID) REFERENCES Route(route_ID);


/* 创建约束条件 */

-- driver的性别字段、出生年龄、号码、身份证号、职位
ALTER TABLE Driver
ADD CONSTRAINT chk_driver_gender
CHECK (driver_gender IN ('M', 'F')),
ADD CONSTRAINT chk_driver_birthday
CHECK (driver_birthday < CURRENT_DATE AND driver_birthday > CURRENT_DATE - INTERVAL '150 years'),
ADD CONSTRAINT chk_driver_phone
CHECK (driver_phone ~ '^[1][0-9]{10}$'),
ADD CONSTRAINT chk_driver_id
CHECK (driver_ID ~ '^[0-9]{17}[0-9Xx]$'),
ADD CONSTRAINT chk_driver_job
CHECK (driver_job IN ('司机', '路队长', '车队队长')),
ADD CONSTRAINT chk_driver_job_and_associated_fields
CHECK (
    (driver_job = '车队队长' AND driver_car_ID IS NULL AND driver_route_ID IS NULL)
    OR
    (driver_job IN ('司机', '路队长') AND driver_car_ID IS NOT NULL AND driver_route_ID IS NOT NULL)
);


-- Car的座位数，车牌号
ALTER TABLE Car
ADD CONSTRAINT chk_car_seat_num
CHECK (car_seat_num > 0),
ADD CONSTRAINT chk_car_id_format
CHECK (car_ID ~ '^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵青藏川宁琼港澳]([A-HJ-NP-Z]{1}[A-Z0-9]{5}|[A-HJ-NP-Z]{2}[A-Z0-9]{4})$');

-- 公司成立时间
ALTER TABLE Company
ADD CONSTRAINT chk_company_establishment_date
CHECK (company_establishment_date < CURRENT_DATE AND company_establishment_date > CURRENT_DATE - INTERVAL '200 years');

-- 违规记录的时间
ALTER TABLE Violation_Record
ADD CONSTRAINT chk_record_time
CHECK (record_time <= CURRENT_TIMESTAMP);



/* 路队长 */
CREATE OR REPLACE FUNCTION get_captain_drivers(captain_company_id INT, captain_work_num VARCHAR)
RETURNS TABLE (
    driver_name VARCHAR(255),
    driver_work_num VARCHAR(20),
    driver_ID CHAR(18),
    car_ID VARCHAR(20),
    driver_phone VARCHAR(15)
) AS $$
BEGIN
    RETURN QUERY
    WITH CaptainInfo AS (
        SELECT 
            f.fleet_company_ID, 
            f.fleet_ID,
            d.driver_route_ID
        FROM 
            Fleet f
        JOIN 
            Driver d ON (f.fleet_company_ID, f.fleet_ID) = (d.driver_fleet_company_ID, d.driver_fleet_ID)
        WHERE 
            d.driver_job = '路队长'
            AND d.driver_fleet_company_ID = captain_company_id
            AND d.driver_work_num = captain_work_num
    )
    SELECT 
        d.driver_name, -- 司机姓名
        d.driver_work_num, -- 工号
        d.driver_ID,
        d.driver_car_ID, -- 车牌号
        d.driver_phone -- 联系方式
    FROM 
        Driver d
    JOIN 
        CaptainInfo ci ON d.driver_fleet_company_ID = ci.fleet_company_ID AND d.driver_fleet_ID = ci.fleet_ID AND d.driver_route_ID = ci.driver_route_ID
    WHERE 
        d.driver_job = '司机'; -- 仅选择职位为“司机”的记录
END;
$$ LANGUAGE plpgsql;


/* 车队队长 */
CREATE OR REPLACE FUNCTION get_fleet_captain_drivers(captain_company_id INT, captain_work_num VARCHAR)
RETURNS TABLE (
    driver_route_ID INT,
    driver_job VARCHAR(255),
    driver_name VARCHAR(255),
    driver_work_num VARCHAR(20),
    car_ID VARCHAR(20),
    driver_phone VARCHAR(15)
) AS $$
BEGIN
    RETURN QUERY
    WITH CaptainInfo AS (
        SELECT 
            f.fleet_company_ID, 
            f.fleet_ID
        FROM 
            Fleet f
        JOIN 
            Driver d ON (f.fleet_company_ID, f.fleet_ID) = (d.driver_fleet_company_ID, d.driver_fleet_ID)
        WHERE 
            d.driver_job = '车队队长'
            AND d.driver_fleet_company_ID = captain_company_id
            AND d.driver_work_num = captain_work_num
    )
    SELECT 
        d.driver_route_ID,
        d.driver_job,
        d.driver_name, -- 司机姓名
        d.driver_work_num, -- 工号
        d.driver_car_ID, -- 车牌号
        d.driver_phone -- 联系方式
    FROM 
        Driver d
    JOIN 
        CaptainInfo ci ON d.driver_fleet_company_ID = ci.fleet_company_ID AND d.driver_fleet_ID = ci.fleet_ID
    where
        d.driver_job = '司机' OR d.driver_job = '路队长'
    ORDER BY 
        d.driver_route_ID ASC; -- 按照线路ID递增排序
END;
$$ LANGUAGE plpgsql;


/* 司机视图 */
CREATE OR REPLACE VIEW Driver_View AS
SELECT 
    d.driver_name,
    d.driver_fleet_company_ID AS company_ID,
    c.company_name,
    d.driver_work_num,
    d.driver_ID,
    d.driver_job,
    d.driver_birthday,
    d.driver_phone,
    d.driver_route_ID,
    d.driver_fleet_ID,
    d.driver_gender,
    c2.car_ID
FROM 
    Driver d
JOIN 
    Company c ON d.driver_fleet_company_ID = c.company_ID
LEFT JOIN 
    Car c2 ON d.driver_car_ID = c2.car_ID;





/* 违章记录视图 */ 
CREATE OR REPLACE VIEW Driver_Violation_View AS
SELECT 
    d.driver_name,
    d.driver_fleet_company_ID AS company_ID,
    d.driver_work_num,
    d.driver_job,
    d.driver_phone,
    d.driver_gender,
    d.driver_fleet_ID,
    d.driver_route_ID,
    v.record_ID,
    v2.violation_name,
    v2.violation_punishment,
    v.record_station_ID,
    v.record_car_ID,
    v.record_time,
    v.record_recorder_ID
FROM 
    Driver d
JOIN 
    Violation_Record v ON d.driver_ID = v.record_driver_ID
JOIN
    Violation v2 ON v.record_violation_ID = v2.violation_ID;