package org.firstinspires.ftc.teamcode;

import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.Locale;

public class Functions {
    private ElapsedTime runtime = new ElapsedTime();
    private BNO055IMU imu;
    Orientation angles;
    Acceleration gravity;
    private Hardware robot;
    SoundPlayer.PlaySoundParams params = new SoundPlayer.PlaySoundParams();


    public enum foundationPos {
        OPEN,
        CLOSED
    }

    public enum direction {
        FORWARD,
        REVERSE
    }

    public enum redOrBlue {
        RED,
        BLUE
    }

    public enum automatic {
        AUTOMATIC,
        MANUAL
    }

    public enum intake {
        IN,
        OUT,
        STOP
    }

    Functions(Hardware robot, BNO055IMU imu) {
        this.robot = robot;
        this.imu = imu;
    }


    static final double COUNTS_PER_MOTOR_REV = 537.6;    // eg: TETRIX Motor Encoder
    static final double DRIVE_GEAR_REDUCTION = 1.0;     // This is < 1.0 if geared UP
    static final double WHEEL_DIAMETER_CENTIMETERS = 10.16;     // For figuring circumference
    static final double COUNTS_PER_CENTIMETER = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_CENTIMETERS * 3.1415);
    static final double DRIVE_SPEED = 1.0;
    static final double TURN_SPEED = 0.8;
    static final double DIST_PER_REV = (4 * 2.54 * Math.PI) / COUNTS_PER_MOTOR_REV;


    public void resetDriveEncoders() {/** DO NOT USE FOR GENERAL PURPOSE **/
        robot.leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void foundationGrabber(foundationPos pose) {
        switch (pose) {
            case OPEN:
                robot.rightFoundation.setPosition(0.1);
                robot.leftFoundation.setPosition(0.9);
                break;
            case CLOSED:
                robot.rightFoundation.setPosition(0.925);
                robot.leftFoundation.setPosition(0.025);
        }

    }

    public void intake(intake pose,
                       double power) {
        switch (pose) {
            case IN:
                robot.rightIntake.setPower(-power);
                robot.leftIntake.setPower(-power);
                break;
            case OUT:
                robot.rightIntake.setPower(power);
                robot.leftIntake.setPower(power);
                break;
            case STOP:
                robot.rightIntake.setPower(0.0);
                robot.leftIntake.setPower(0.0);
        }
    }

    public void gyroDrive(double heading,
                          double distanceCM,
                          double power,
                          double timeoutS) {

        double currentHeading;
        double drvPower = power;
        double adjPower = 0;
        double currentDistance;
        double distanceTicks;
        runtime.reset();
        while (runtime.seconds() < timeoutS) {
            currentHeading = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
            //  adjPower = (currentHeading - heading) / 500;
            currentDistance = robot.leftDrive.getCurrentPosition();
            distanceTicks = (distanceCM * COUNTS_PER_CENTIMETER);
            if (currentDistance > distanceTicks) {
                robot.leftDrive.setPower(0);
                robot.leftBackDrive.setPower(0);
                robot.rightDrive.setPower(0);
                robot.rightBackDrive.setPower(0);
                return;
            }
            robot.leftDrive.setPower(drvPower - adjPower);
            robot.leftBackDrive.setPower(drvPower - adjPower);
            robot.rightDrive.setPower(drvPower + adjPower);
            robot.rightBackDrive.setPower(drvPower + adjPower);

        }
    }


    public void gyroTurn(double heading,
                         double timeoutS) {
        double currentHeading;
        double power;
//        double p = 0;
//        double i = 0;
//        double d = 0;
//        double pK = .05;
//        double iK = .5;
//        double dK = .05;
//        double oldHeading = 0;
        runtime.reset();
        while (runtime.seconds() < timeoutS) {
            currentHeading = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
//            p = (currentHeading - heading) * pK;
//            i = p * iK + i;
//            d = (oldHeading - currentHeading) * dK;
//            power = p+i+d;
            power = (currentHeading - heading) / 50;
            if (power < 0) {
                power = -Math.max(0.1, Math.abs(power));
            } else {
                power = Math.max(0.1, Math.abs(power));
            }
            if (Math.abs(currentHeading - heading) < 2.0) {
                return;
            }

            robot.leftDrive.setPower(-power);
            robot.leftBackDrive.setPower(-power);
            robot.rightDrive.setPower(power);
            robot.rightBackDrive.setPower(power);
        }
    }

    public double subtractAngle(double angleA,
                                double angleB) {
        double result;
        result = angleA - angleB;
        if (result > 180) {
            result = result - 360;
        }
        return result;
    }

    public void gyroStrafe(double heading,
                           double pose,
                           double distance,
                           double power,
                           double timeoutS) {

        double currentHeading;
        double drvPower = power;
        double adjPower;
        double currentDistance;
        double driveAngle;
        double headingRadians;
        double poseDegrees;
        double distX = 0;
        double distY = 0;
        double dY;
        double dX;
        double oldLeft = robot.leftDrive.getCurrentPosition();
        double oldRight = robot.rightDrive.getCurrentPosition();
        double oldBackLeft = robot.leftBackDrive.getCurrentPosition();
        double oldBackRight = robot.rightBackDrive.getCurrentPosition();
        double newLeft = 0;
        double newRight = 0;
        double newBackLeft = 0;
        double newBackRight = 0;
        runtime.reset();
        while ((runtime.seconds() < timeoutS)) {
            currentHeading = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
            headingRadians = ((-currentHeading / 180) * 3.1416) + (1 / 2 * 3.1416);
            poseDegrees = ((pose - 3.1416 / 2) % (2 * 3.1416)) * (360 / (2 * 3.1416));
            if (poseDegrees > 180) {
                poseDegrees -= 360;
            }
            if (poseDegrees < -180) {
                poseDegrees += 360;
            }
            driveAngle = subtractAngle(poseDegrees, currentHeading);
            adjPower = subtractAngle(poseDegrees, currentHeading) / 120;
            newLeft = robot.leftDrive.getCurrentPosition();
            newRight = robot.rightDrive.getCurrentPosition();
            newBackRight = robot.rightBackDrive.getCurrentPosition();
            newBackLeft = robot.leftBackDrive.getCurrentPosition();

            dX = ((((newLeft - oldLeft) - (newBackLeft - oldBackLeft)
                    - (newRight - oldRight) + (newBackRight - oldBackRight)) * Math.sin(Math.PI / 4)) / 4.0) * DIST_PER_REV;
            dY = (((newLeft - oldLeft) + (newBackLeft - oldBackLeft)
                    + (newRight - oldRight) + (newBackRight - oldBackRight)) / 4.0) * DIST_PER_REV;
            distX += (Math.sin(headingRadians) * dX + Math.cos(headingRadians) * dY);
            distY += (Math.sin(headingRadians) * dY + Math.cos(headingRadians) * dX);
            oldLeft = newLeft;
            oldRight = newRight;
            oldBackLeft = newBackLeft;
            oldBackRight = newBackRight;

            currentDistance = Math.sqrt(distX * distX + distY * distY);
            if (currentDistance > distance) {
                robot.leftDrive.setPower(0);
                robot.leftBackDrive.setPower(0);
                robot.rightDrive.setPower(0);
                robot.rightBackDrive.setPower(0);
                return;
            }

            driveAngle = ((-currentHeading / 180) * 3.1416) + (1 / 2 * 3.1416) + heading;
//            driveAngle = 0.5*Math.PI;
//            adjPower = 0;

            double robotAngle = driveAngle - Math.PI / 4;
            final double v1 = power * Math.cos(robotAngle) - adjPower;
            final double v2 = power * Math.sin(robotAngle) + adjPower;
            final double v3 = power * Math.sin(robotAngle) - adjPower;
            final double v4 = power * Math.cos(robotAngle) + adjPower;


            robot.leftDrive.setPower(v1);
            robot.rightDrive.setPower(v2);
            robot.leftBackDrive.setPower(v3);
            robot.rightBackDrive.setPower(v4);
        }
    }

    public void continuousGyroStrafe(double heading,
                                     double pose,
                                     double power) {
        double currentHeading;
        double adjPower;
        double driveAngle;
        double headingRadians;
        double poseDegrees;

        currentHeading = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
        poseDegrees = ((pose - 3.1416 / 2) % (2 * 3.1416)) * (360 / (2 * 3.1416));
        if (poseDegrees > 180) {
            poseDegrees -= 360;
        }
        if (poseDegrees < -180) {
            poseDegrees += 360;
        }
        adjPower = subtractAngle(poseDegrees, currentHeading) / 120;
        driveAngle = ((-currentHeading / 180) * 3.1416) + (1 / 2 * 3.1416) + heading;
//            driveAngle = 0.5*Math.PI;
//            adjPower = 0;
        double robotAngle = driveAngle - Math.PI / 4;
        final double v1 = power * Math.cos(robotAngle) - adjPower;
        final double v2 = power * Math.sin(robotAngle) + adjPower;
        final double v3 = power * Math.sin(robotAngle) - adjPower;
        final double v4 = power * Math.cos(robotAngle) + adjPower;
        robot.leftDrive.setPower(v1);
        robot.rightDrive.setPower(v2);
        robot.leftBackDrive.setPower(v3);
        robot.rightBackDrive.setPower(v4);
        return;
    }

    public void brake() {
        robot.leftDrive.setPower(0);
        robot.leftBackDrive.setPower(0);
        robot.rightDrive.setPower(0);
        robot.rightBackDrive.setPower(0);
        return;
    }

    public void encoderDrive(double speed,
                             double leftCentimeters, double rightCentimeters,
                             double timeoutS) {
        int newLeftTarget;
        int newRightTarget;

        // Ensure that the opmode is still active
        if (true) {

            // Determine new target position, and pass to motor controller
            newLeftTarget = robot.leftDrive.getCurrentPosition() + (int) (leftCentimeters * COUNTS_PER_CENTIMETER);
            newRightTarget = robot.rightDrive.getCurrentPosition() + (int) (rightCentimeters * COUNTS_PER_CENTIMETER);
            robot.leftDrive.setTargetPosition(newLeftTarget);
            robot.leftBackDrive.setTargetPosition(newLeftTarget);
            robot.rightDrive.setTargetPosition(newRightTarget);
            robot.rightBackDrive.setTargetPosition(newRightTarget);

            // Turn On RUN_TO_POSITION
            robot.leftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.leftBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rightBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            robot.leftDrive.setPower(Math.abs(speed));
            robot.leftBackDrive.setPower(Math.abs(speed));
            robot.rightDrive.setPower(Math.abs(speed));
            robot.rightBackDrive.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.

            // Stop all motion;
            robot.leftDrive.setPower(0);
            robot.leftBackDrive.setPower(0);
            robot.rightDrive.setPower(0);
            robot.rightBackDrive.setPower(0);

            // Turn off RUN_TO_POSITION
            robot.leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            //  sleep(250);   // optional pause after each move
        }
    }

    public void waitMilis(double timeOutMs) {

        runtime.reset();
        while (runtime.milliseconds() < timeOutMs) ;
    }

    public void stoneDetectionBlue() {
        double timeoutS = 7.0;
        double hueValue = 0.2;
        double redU = 0.0;
        double greenU = 0.0;
        double blueU = 0.0;
        boolean foundPos = false;
        int run = 0;
        int zero = 0;
        runtime.reset();
        while (robot.rightRangeSensor.getDistance(DistanceUnit.CM) > 22) {
            continuousGyroStrafe(.6,1.57,.6);
        }
        while (robot.rightColorSensor.getDistance(DistanceUnit.CM) > 10) {
            continuousGyroStrafe(1.57, 1.57, .4);
        }
        brake();
        //gyroStrafe(1.571, 1.571, 40, 0.6, 10);
        // gyroStrafe(0, 1.571, 35, 0.6, 10);
        while (timeoutS > runtime.seconds() && !foundPos) {
            redU = (double) robot.rightColorSensor.red() / (double) robot.rightColorSensor.alpha();
            greenU = (double) robot.rightColorSensor.green() / (double) robot.rightColorSensor.alpha();
            blueU = (double) robot.rightColorSensor.blue() / (double) robot.rightColorSensor.alpha();
            //Sees skystone
            //stafe to pos?
            if (((greenU + redU) * hueValue) < blueU) {
                //Sees skystone

                //Sees skystone
                //stafe to pos?
                foundPos = true;
                brake();
                gyroStrafe(0.0, 1.57, 5, 0.4, 5);
                intake(intake.IN, 0.8);
                gyroStrafe(1.57, 1.57, 20, 0.25, 8);
                waitMilis(250);
                robot.conveyorServo.setPower(1);
//                gyroStrafe(4.71, 1.57, 35, 0.7, 8);

                return;
            } else {
                continuousGyroStrafe(3.14, 1.57, .4);
                run++;
            }
        }
    }

    public void stoneDetectionRed() {
        double timeoutS = 7.0;
        double hueValue = 0.2;
        double redU = 0.0;
        double greenU = 0.0;
        double blueU = 0.0;
        boolean foundPos = false;
        int run = 0;
        runtime.reset();
        while (robot.leftRangeSensor.getDistance(DistanceUnit.CM) > 22) {
            continuousGyroStrafe(2.54,1.57,.6);
        }
        while (robot.rightColorSensor.getDistance(DistanceUnit.CM) > 12) {
            continuousGyroStrafe(1.57, 1.57, .4);
        }
        brake();
        //gyroStrafe(1.571, 1.571, 40, 0.6, 10);
        // gyroStrafe(0, 1.571, 35, 0.6, 10);
        while (timeoutS > runtime.seconds() && !foundPos) {
            redU = (double) robot.leftColorSensor.red() / (double) robot.leftColorSensor.alpha();
            greenU = (double) robot.leftColorSensor.green() / (double) robot.leftColorSensor.alpha();
            blueU = (double) robot.leftColorSensor.blue() / (double) robot.leftColorSensor.alpha();
            //Sees skystone
            //stafe to pos?
            if (((greenU + redU) * hueValue) < blueU) {
                //Sees skystone

                //Sees skystone
                //stafe to pos?
                foundPos = true;
                brake();
                gyroStrafe(3.14, 1.57, 5, 0.4, 5);
                intake(intake.IN, 0.8);
                gyroStrafe(1.57, 1.57, 20, 0.25, 8);
                waitMilis(250);
                robot.conveyorServo.setPower(1);
//                gyroStrafe(4.71, 1.57, 35, 0.7, 8);

                return;
            } else {
                continuousGyroStrafe(0.0, 1.57, .4);
                run++;
            }
        }

    }

    public void velocityControl(int ticks) {
        while (robot.leftBackDrive.getCurrentPosition() < ticks) {
            robot.leftBackDrive.setPower(0.2);
        }
        robot.leftBackDrive.setPower(0.0);
    }


    public void autonomousParking(
            redOrBlue color,
            double heading,
            double pose,
            boolean stopper,
            double timeoutS) {
        double hueValueBlue = 0.5;
        double hueValueRed = 0.6;
        double power = 0.35;
        double redPower = .3;
        double distance = 2;
        double motorTimeOutS = 2;
        double redU = 0.0;
        double greenU = 0.0;
        double blueU = 0.0;
        boolean foundRed = false;
        boolean foundBlue = false;

        runtime.reset();

        switch (color) {
            case RED:
                while ((!foundRed) && (timeoutS > runtime.seconds()) && stopper) {
                    redU = (double) robot.bottomColorSensor.red() / (double) robot.bottomColorSensor.alpha();
                    greenU = (double) robot.bottomColorSensor.green() / (double) robot.bottomColorSensor.alpha();
                    blueU = (double) robot.bottomColorSensor.blue() / (double) robot.bottomColorSensor.alpha();
                    if (.4 <= redU) {
                        robot.leftDrive.setPower(0.0);
                        robot.leftBackDrive.setPower(0.0);
                        robot.rightDrive.setPower(0.0);
                        robot.rightBackDrive.setPower(0.0);
                        foundRed = true;
                    } else {
                        continuousGyroStrafe(heading, pose, redPower);
                    }
                }
                break;
            case BLUE:
                while ((!foundBlue) && (timeoutS > runtime.seconds()) && stopper) {
                    redU = (double) robot.bottomColorSensor.red() / (double) robot.bottomColorSensor.alpha();
                    greenU = (double) robot.bottomColorSensor.green() / (double) robot.bottomColorSensor.alpha();
                    blueU = (double) robot.bottomColorSensor.blue() / (double) robot.bottomColorSensor.alpha();
                    if (((greenU + redU) * hueValueBlue) < blueU) {
                        robot.leftDrive.setPower(0.0);
                        robot.leftBackDrive.setPower(0.0);
                        robot.rightDrive.setPower(0.0);
                        robot.rightBackDrive.setPower(0.0);
                        foundBlue = true;
                    } else {
                        continuousGyroStrafe(heading, pose, power);
                    }
                }
                break;
        }

    }

    public void foundationLinerUpper(double power) {

        robot.lift.setTargetPosition(-350);
        robot.lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.lift.setPower(.7);
        robot.leftFoundation.setPosition(0);
        robot.rightFoundation.setPosition(1.0);
        while ((robot.leftFoundationSwitch.getVoltage() < 3.0) || (robot.rightFoundationSwitch.getVoltage() < 3.0)) {
            if (robot.leftFoundationSwitch.getVoltage() < 3.0) {
                robot.leftDrive.setPower(-power);
                robot.leftBackDrive.setPower(-power);
            } else {
                robot.leftDrive.setPower(0);
                robot.leftBackDrive.setPower(0);
            }
            if (robot.rightFoundationSwitch.getVoltage() < 3.0) {
                robot.rightDrive.setPower(-power);
                robot.rightBackDrive.setPower(-power);
            } else {
                robot.rightDrive.setPower(0);
                robot.rightBackDrive.setPower(0);
            }
        }
        return;
    }

    public void resetRobotEncoders(Telemetry telemetry) {
        telemetry.addData("Initialized", "Resetting Encoders");
        telemetry.update();
        robot.leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        waitMilis(100);
        telemetry.addData("Resetting Silver Platter", "Resetting Encoders");
        telemetry.update();
        robot.lift.setPower(-.95);
        waitMilis(150);
        robot.lift.setPower(-.2);
        robot.silverPlatter.setPower(.3);
        waitMilis(200);
        robot.silverPlatter.setPower(-0.4);
        waitMilis(150);
        while (robot.retractedSwitch.getVoltage() < 3.3) {
            robot.silverPlatter.setPower(-0.3);
        }
        waitMilis(10);
        robot.silverPlatter.setPower(0.0);
        telemetry.addData("Silver Platter Reset", "Resetting Encoders");
        telemetry.update();
        robot.silverPlatter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.silverPlatter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        waitMilis(100);
        telemetry.addData("Resetting Lift", "Resetting Encoders");
        robot.lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        while (robot.liftSwitch.getVoltage() < 3.3) {
            robot.lift.setPower(0.0);
        }
        waitMilis(0);
        robot.lift.setPower(0.0);
        robot.lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        telemetry.addData("Lift Reset", "Resetting Encoders");
        telemetry.update();
        waitMilis(0);
        telemetry.addData("Resume Playing", "Have a nice day");
        telemetry.update();

    }
}