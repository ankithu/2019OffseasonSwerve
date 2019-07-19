
package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.revrobotics.CANEncoder;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class SwerveModule {
    private static final double kTurnEncoderClicksperRevolution = 1024.0; 
    private static final double kWheelDiameter = 4.0;
    private static final double kAzimuthDegreesPerClick = 360.0/kTurnEncoderClicksperRevolution;
    private static final double kAzimuthClicksPerDegree = kTurnEncoderClicksperRevolution/360.0;
    private static final int kSlotIdx = 0;
    private static final int kTimeoutMs = 30;

    private CANSparkMax driveMotor;  
    private TalonSRX turnMotor;
    private CANEncoder motorEncoder;

    //Swerve Module Specific - must be changed for each swerve module !!!!!
    private double kP;
    private double kI; 
    private double kD;  
    private double kF; 
    private int kBaseEncoderClicks;
    private int kMagicCruiseVelocity;
    private int kMagicCruiseAcceleration; 
    private boolean kTurnCountsDecreasing; 
    private boolean kDriveMotorInverted; 
    private boolean kDriveEncoderInverted;
    private boolean kTurnMotorInverted;
    private boolean kTurnEncoderInverted; 

    public SwerveModule (int driveMotorID,int turnMotorID, double P, double I, double D, double F,
        int startingEncoderClick,
        int cruiseVelocity, int cruiseAccel, 
        boolean turnCountsDecreasing, boolean DriveInverted, boolean DriveEncoderInverted, 
        boolean TurnMotorInverted, boolean TurnEncoderInverted) {
       
        this.driveMotor = new CANSparkMax(driveMotorID, MotorType.kBrushless);  
        this.motorEncoder = new CANEncoder(this.driveMotor);
        this.turnMotor = new TalonSRX(turnMotorID);

        driveMotor.setIdleMode(IdleMode.kBrake);
        turnMotor.setNeutralMode(NeutralMode.Brake);
        //this is the encoder count when the wheel is aligned forward at the start 
        this.kBaseEncoderClicks = startingEncoderClick;
        this.kMagicCruiseVelocity = cruiseVelocity;
        this.kMagicCruiseAcceleration = cruiseAccel;
        this.kTurnCountsDecreasing = turnCountsDecreasing;
        this.kDriveMotorInverted = DriveInverted; 
        this.kDriveEncoderInverted = DriveEncoderInverted;
        this.kTurnMotorInverted = TurnMotorInverted;
        this.kTurnEncoderInverted = TurnEncoderInverted;
        this.kP = P;
        this.kI = I; 
        this.kD = D; 
        this.kF = F;

        //configure drive motor  
        driveMotor.setInverted(kDriveMotorInverted); 
    
        //configure turn motor 
        turnMotor.configSelectedFeedbackSensor(FeedbackDevice.Analog);
        turnMotor.configFeedbackNotContinuous(true,kTimeoutMs);
    
        //set to true to invert sensor   
        turnMotor.setInverted(kTurnMotorInverted);
        turnMotor.setSensorPhase(kTurnEncoderInverted);

        turnMotor.configNominalOutputForward(0,kTimeoutMs);
        turnMotor.configNominalOutputReverse(0,kTimeoutMs);
        turnMotor.configPeakOutputForward(1,kTimeoutMs);
        turnMotor.configPeakOutputReverse(-1,kTimeoutMs);
        turnMotor.selectProfileSlot(kSlotIdx, 0);  // slot index = 0 , pidloopidx = 0 
        turnMotor.config_kF(kSlotIdx, kF,kTimeoutMs);
        turnMotor.config_kP(kSlotIdx, kP,kTimeoutMs);
        turnMotor.config_kI(kSlotIdx, kI,kTimeoutMs);
        turnMotor.config_kD(kSlotIdx, kD,kTimeoutMs);
        turnMotor.configMotionCruiseVelocity(kMagicCruiseVelocity,kTimeoutMs);
        turnMotor.configMotionAcceleration(kMagicCruiseAcceleration, kTimeoutMs);
      }
    


    public void drive(double speed, double angle) { 
        driveMotor.set(speed); 
       
        //angle is bound to -180 - +180 and degrees are from 0-360
        //convert bounded angle into true compass degrees 
        double trueAngle = angle; 
        if(angle < 0) {
            trueAngle = 180 + (180+angle);
        }

        //convert angle (0-360) into encoder clicks (0-1024) 
        int desiredclicks = (int) Math.round(trueAngle * kAzimuthClicksPerDegree);
    
        if(kTurnCountsDecreasing) {
            //this means a positive right turn mean decreasing encoder counts 
            desiredclicks = kBaseEncoderClicks -  desiredclicks ;
            if(desiredclicks<0) {
                desiredclicks += kTurnEncoderClicksperRevolution;
            }

        } else {
            //this means a positive right trn with increasing encoder counts 
            //addin the base starting clicks when the wheel is pointing to zero
            desiredclicks += kBaseEncoderClicks;
            //becuase we are using an absolute encoder the value must be between 0 and 1024 
            if(desiredclicks > 1024) {
                desiredclicks -= kTurnEncoderClicksperRevolution;
            }
        }  

        

        turnMotor.set(ControlMode.MotionMagic, desiredclicks);
    }

    public void resetDriveEncoder() {
        motorEncoder.setPosition(0.0);
    }

    public double getDriveEncoderPosition() {
        double pos = motorEncoder.getPosition();
    //    if(kDriveEncoderInverted) { 
    //      pos *= -1;
    //    }
         return pos; 
       }

    public double getDriveEncoderVelocity() {
        double vol =motorEncoder.getVelocity();
    //    if(kDriveEncoderInverted) { 
    //      vol *= -1;
    //    }
         return vol; 
       }

    public double getTurnEncoderPosition() {
        return turnMotor.getSelectedSensorPosition(0);
    }

    public double getTurnClosedLoopError() {
        return turnMotor.getClosedLoopError();
      }

    public double getTurnEncoderPositioninDegrees() {
        //get the base clicks = zero degrees for this particular wheel 
        
        double pos = turnMotor.getSelectedSensorPosition(0);
        //relative position 
        double relpos = 0; 
        //actual position 
        int actpos;

        if(kTurnCountsDecreasing) {
            //this means a positive right turn means decreasing encoder counts 
            relpos = (kBaseEncoderClicks - pos);
        } else {
            //this means a positive right turn with increasing encoder counts 
            relpos = (pos - kBaseEncoderClicks); 
        }

        //adjust for absolute encoder with 0-1024 clicks
        if(relpos<0) {
            relpos+=kTurnEncoderClicksperRevolution;
        }
        actpos = (int) Math.round(relpos * kAzimuthDegreesPerClick);
        
        if(actpos == 360 ) {
            actpos = 0;
        }
        
        return actpos; 
    }
    


    /********************************************************************************
    * Section - Encoder Conversion Routines
    *******************************************************************************/

    private static double ticksToInches(double ticks) {
        return rotationsToInches(ticksToRotations(ticks));
    }

    private static double rotationsToInches(double rotations) {
        return rotations * (kWheelDiameter * Math.PI);
    }

    private static double ticksToRotations(double ticks) {
        int kEncoderUnitsPerRev = 42; // Rev Native Internal Encoder clicks per revolution  
        return ticks / kEncoderUnitsPerRev;
    }

    private static double inchesToRotations(double inches) {
        return inches / (kWheelDiameter * Math.PI);
    }
}