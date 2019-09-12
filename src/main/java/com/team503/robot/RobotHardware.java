/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.team503.robot;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.team503.lib.controllers.PurePursuitController.Lookahead;
import com.team503.lib.geometry.Translation2d;
import com.team503.lib.util.Util;
import com.team503.robot.RobotState.Bot;

/**
 * Add your docs here.
 */
public abstract class RobotHardware {

    public abstract void initalizeConstants();

    // Constants

    // SwerveFileNames
    public abstract String getBackLeftName();

    public abstract String getBackRightName();

    public abstract String getFrontLeftName();

    public abstract String getFrontRightName();

    // Gamespec
    public abstract boolean hasArm();

    public abstract boolean hasWrist();

    public abstract boolean hasIntake();

    public abstract boolean hasCompressor();

    public abstract boolean hasExtension();

    public abstract boolean hasLimelight();


    public static final boolean kDebuggingOutput = true;

    // Limelight Constants
    public double visionAreaConstant = 1.0;
    public double yVisionkP = 0.57;
    public double xVisionkP = 3.0;

    // Swerve Calculations Constants (measurements are in inches)
    public double kWheelbaseLength;
    public double kWheelbaseWidth;
    public double wheelDiameter;
    public double kTurnEncoderClicksperRevolution;
    public double requestDriveReversed;

    public int requestPigeonFlipped;

    // Swerve Module Positions (relative to the center of the drive base)
    public Translation2d kVehicleToFrontRight;
    public Translation2d kVehicleToBackRight;
    public Translation2d kVehicleToFrontLeft;
    public Translation2d kVehicleToBackLeft;

    public Translation2d[] kModulePositions;

    public final String motionProfilingRioFolder = "/home/lvuser/MotionProfiles/";

    // Pure Pursuit
    public final double POSE_LOOP_DT = 0.01;

    public double kMinLookAhead;
    public double kMinLookAheadSpeed;
    public double kMaxLookAhead;
    public double kMaxLookAheadSpeed;

    public double kPathFollowingMaxAccel;
    public double kPathFollowingMaxVel;

    public double kPurePursuitP;
    public double kPurePursuitV;

    /* Gamespec vars */

    public int kEncoderUnitsPerRev;

    public int gTimeoutMs;
    public int gSlotIdx;

    // Arm
    public int armMasterID;
    public int armSlaveID;
    public int kArmCruiseVel;
    public int kArmAcceleration;

    public double kArmF;
    public double kArmP;
    public double kArmI;
    public double kArmD;

    public double gArmAngularOffset;

    public boolean armMasterInverted;
    public boolean armSlaveInverted;
    public boolean armMasterSensorPhase;

    // Wrist/Intake
    public int rollerIntakeID;

    public int intakePdpChannel;
    public int vacuumPdpChannel;

    public double intakePower;
    public double intakeStallPower;
    public double intakeOutPower;
    public double intakeVaccPower;

    public int hatchVacId;
    public int releaseId;

    public double rollerCurrentThres;
    public double vacuumCurrentThres;

    public int wristID;

    public double kWristCruiseVel;
    public double kWristAcceleration;

    public double kWristF;
    public double kWristP;
    public double kWristI;
    public double kWristD;

    public double gWristMinLimit;
    public double gWristMaxLimit;

    public double gWristMaxLimitCargo;

    public double gWristAngularOffset;
    public double gWristGroundOffset;

    public boolean wristMotorInverted;

    public boolean wristSensorPhase;
    public double MAX_WRIST_POWER;

    // Extension
    public int extensionID;

    public boolean extensionSensorPhase;
    public boolean extensionMotorInverted;

    public double kExtF;
    public double kExtP;
    public double kExtI;
    public double kExtD;

    public int kExtCruiseVel;
    public int kExtAcceleration;

    public double gExtGearRatio;
    public double gExtSpoolDiameter;
    public double gExtOffset;
    public double gExtMinLim;
    public double gExtMaxLim;

    public double gArmExtLength;

    // Power Distribution Panel
    public int PdpID;



    ///////////////////////////////////////////////////////////////////////////////////

    // FF Swerve Constants

    // ARM
    public static final int ARM = 24; //TODO

    // Constraints
    public static final double kArmMaxControlAngle = 0; //degrees //TODO
    public static final double kArmMinControlAngle = 0; //degrees //TODO
    public static final double kArmMaxPhysicalAngle = 0; //degrees //TODO
    public static final double kArmMinPhysicalAngle = 0; //degrees //TODO
    public static final double kArmMaxCurrent = 40.0; //amps //TODO

    // Motion Control
    public static final double kArmMaxSpeed = 1000; //enc units per 100 MS //TODO
    public static final double kArmAngleTolerance = 5; //degrees //TODO

    // Position-Encoder relations
    public static final double kArmEncoderToOutputRatio = 1.0; //arm rev per enc rev //TODO
    public static final int kArmStartingAngle = 0; //physical starting angle (degrees) //TODO
    public static final int kArmStartingEncoderPosition = 0; //starting encoder counts (enc units) //TODO


    // ELEVATOR
    public static final int ELEVATOR = 25; //TODO
    
    public static final double kElevatorTeleopManualSpeed = 0.5; // Percent max output manual //TODO

    // Constraints
    public static final double kElevatorMinHeight = 50.0; //inches //TODO
    public static final double kElevatorMaxHeight = 50.0; //inches //TODO
    public static final int kElevatorCurrentLimit = 40; //amps //TODO
    public static final int kElevatorMaxCurrent = 50; //amps //TODO

    // Motion Control
    public static final double kElevatorMaxSpeed = 1000; //rev per min //TODO
    public static final double kElevatorHeightTolerance = 5; //inches //TODO

    // Position-Encoder Relations
    public static double kElevatorTicksPerInch = 4332; // enc counts per inch //TODO
    public static double kElevatorEncoderStartingPosition = 0; // enc counts //TODO
    public static double kElevatorMaxInitialHeight = 7.0; //inches //TODO
    public static double kElevatorMinInitialHeight = 7.0; //inches //TODO

    public final Lookahead getLookahead() {
        final Lookahead lookahead = new Lookahead(kMinLookAhead, kMaxLookAhead, kMinLookAheadSpeed, kMaxLookAheadSpeed);
        return lookahead;
    }

    /**
     * @return the MAC address of the robot
     */
    public static String getMACAddress() {
        try {
            Enumeration<NetworkInterface> nwInterface = NetworkInterface.getNetworkInterfaces();
            StringBuilder ret = new StringBuilder();
            while (nwInterface.hasMoreElements()) {
                NetworkInterface nis = nwInterface.nextElement();
                if (nis != null) {
                    byte[] mac = nis.getHardwareAddress();
                    if (mac != null) {
                        for (int i = 0; i < mac.length; i++) {
                            ret.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                        }
                        return ret.toString();
                    } else {
                        System.out.println("Address doesn't exist or is not accessible");
                    }
                } else {
                    System.out.println("Network Interface for the specified address is not found.");
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static RobotHardware instance = null;

    public static RobotHardware getInstance() {
        if (instance == null) {
            if (RobotState.getInstance().getCurrentRobot().equals(Bot.Automatic)) {
                RobotState.getInstance().setCurrentRobot(Util.parseRobotNameToEnum(Util.readRobotName()));
            }
            switch (RobotState.getInstance().getCurrentRobot()) {
            case ProgrammingBot:
                instance = new RobotHardwareProgammingBot();
                break;
            case FFSwerve:
                instance = new FFRobotHardware();
                break;
            case Automatic:
                System.err.println("Robot should not be set to automatic... something went wrong");
                break;
            }
            instance.initalizeConstants();
            // Util.setPseudoInverseForwardKinematicsMatrix();
        }
        return instance;
    }

}
