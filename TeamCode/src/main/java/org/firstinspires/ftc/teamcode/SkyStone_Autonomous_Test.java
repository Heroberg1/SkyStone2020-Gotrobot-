package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

/**
 * This file illustrates the concept of driving a path based on encoder counts.
 * It uses the common Pushbot hardware class to define the drive on the robot.
 * The code is structured as a LinearOpMode
 * <p>
 * The code REQUIRES that you DO have encoders on the wheels,
 * otherwise you would use: PushbotAutoDriveByTime;
 * <p>
 * This code ALSO requires that the drive Motors have been configured such that a positive
 * power command moves them forwards, and causes the encoders to count UP.
 * <p>
 * The desired path in this example is:
 * - Drive forward for 48 inches
 * - Spin right for 12 Inches
 * - Drive Backwards for 24 inches
 * - Stop and close the claw.
 * <p>
 * The code is written using a method called: encoderDrive(speed, leftMillimeters, rightMillimeters, timeoutS)
 * that performs the actual movement.
 * This methods assumes that each movement is relative to the last stopping place.
 * There are other ways to perform encoder based moves, but this method is probably the simplest.
 * This code uses the RUN_TO_POSITION mode to enable the Motor controllers to generate the run profile
 * <p>
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@Autonomous(name = "AutonomousTest", group = "OpMode")
//@Disabled
public class SkyStone_Autonomous_Test extends LinearOpMode {

    /* Declare OpMode members. */
    BNO055IMU imu;
    Hardware robot = new Hardware();   // Use a Pushbot's hardware
    private ElapsedTime runtime = new ElapsedTime();
    Orientation angles;
    Acceleration gravity;


    static final double COUNTS_PER_MOTOR_REV = 1120;    // eg: TETRIX Motor Encoder
    static final double DRIVE_GEAR_REDUCTION = 1.0;     // This is < 1.0 if geared UP
    static final double WHEEL_DIAMETER_CENTIMETERS = 10.16;     // For figuring circumference
    static final double COUNTS_PER_CENTIMETER = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_CENTIMETERS * 3.1415);
    static final double DRIVE_SPEED = 1.0;
    static final double TURN_SPEED = 0.8;
    static final double DIST_PER_REV = (4 * 2.54 * Math.PI) / 1120;

    @Override
    public void runOpMode() {
        /*
         * Initialize the drive system variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap);
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        Functions fun = new Functions(robot, imu);
        fun.waitMilis(50);
        // Send telemetry message to signify robot waiting;

        // Send telemetry message to indicate successful Encoder reset
//        telemetry.addData("Path0", "Starting at %7d :%7d",
//                robot.leftDrive.getCurrentPosition(),
//                robot.rightDrive.getCurrentPosition());
//        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();
//        while (opModeIsActive()) {
//
//        }
//        robot.leftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        robot.rightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        robot.rightBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        robot.leftBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        fun.waitMilis(100);
        while (opModeIsActive()) {
            robot.conveyorServo.setPower(-1);
            fun.intake(Functions.intake.IN,.8);
            if (gamepad2.a) {
                fun.intake(Functions.intake.STOP,0);
                robot.conveyorServo.setPower(1);
                fun.gyroStrafe(4.71,1.57,15,1,3);
            }
//            robot.leftDrive.setPower(.2);
//            robot.rightDrive.setPower(.2);
//            robot.leftBackDrive.setPower(.2);
//            robot.rightBackDrive.setPower(.2);
//            telemetry.addData("fl", robot.leftDrive.getCurrentPosition());
//            telemetry.addData("fr", robot.rightDrive.getCurrentPosition());
//            telemetry.addData("bl", robot.leftBackDrive.getCurrentPosition());
//            telemetry.addData("br", robot.rightBackDrive.getCurrentPosition());
//            telemetry.update();
//            fun.waitMilis(50);
        }
        // fun.autonomousParking(Functions.direction.FORWARD, Functions.redOrBlue.BLUE);
        // fun.foundationLinerUpper(.5);
/** Foundation line up test **/
     /*   robot.lift.setTargetPosition(-350);
        robot.lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.lift.setPower(.8);
        robot.leftFoundation.setPosition(0.0);
        robot.rightFoundation.setPosition(1.0);
        fun.waitMilis(100);
        fun.foundationLinerUpper(.2);
*/


//}


        // fun.resetRobotEncoders(telemetry);


        //  fun.gyroStrafe(0.0,1.57,20,0.4,4);

//        while (opModeIsActive()) {
//            if (gamepad2.a) {
//                robot.leftDrive.setPower(0.5);
//            } else {
//                robot.leftDrive.setPower(0.0);
//            }
//            if (gamepad2.y) {
//                robot.leftBackDrive.setPower(0.5);
//            } else {
//                robot.leftBackDrive.setPower(0.0);
//            }
//            if (gamepad2.b) {
//                robot.rightDrive.setPower(0.5);
//            } else {
//                robot.rightDrive.setPower(0.0);
//            }
//            if (gamepad2.x) {
//                robot.rightBackDrive.setPower(0.5);
//            } else {
//                robot.rightBackDrive.setPower(0.0);
//            }
//        }


        /*
         *  Method to perform a relative move, based on encoder counts.
         *  Encoders are not reset as the move is based on the current position.
         *  Move will stop if any of three conditions occur:
         *  1) Move gets to the desired position
         *  2) Move runs out of time
         *  3) Driver stops the opmode running.
         */


    }
}
