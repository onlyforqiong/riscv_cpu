
package examples

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._



class if2id(length_width :Int,bank_width:Int) extends Module with riscv_macros{
    val io = IO(new Bundle { //有隐式的时钟与复位，并且复位为高电平复位
        //流水线中的延迟器
        val en  = Input(UInt(1.W))
        val clr  = Input(UInt(1.W))

        val InstrF = Input(UInt(data_length.W))
        // val read_bank_pointF = Input(UInt(bank_width.W))
        // val read_length_pointF = Input(UInt(length_width.W))
        val PCPlus4F  = Input(UInt(data_length.W))
        val PCPlus8F  = Input(UInt(data_length.W))
        val PCF       = Input(UInt(data_length.W))
        //这里只处理有关tlb例外有关的东西
        val ExceptionTypeF = Input(UInt(2.W))
        val NextDelaySlotD = Input(UInt(1.W))

        val InstrD   = Output(UInt(32.W))
        val PCPlus4D = Output(UInt(data_length.W))
        val PCPlus8D = Output(UInt(data_length.W))
        val InDelaySlotD = Output(UInt(1.W)) //延迟时隙
        val PCD      = Output(UInt(data_length.W))
        val ExceptionTypeD_Out = Output(UInt(2.W))
        // val read_bank_pointD = Output(UInt(bank_width.W))
        // val read_length_pointD = Output(UInt(length_width.W))

    })
    val InstrD_Reg = RegInit("b0010011".U(32.W))
    val PCPlus4D_Reg = RegInit(0.U(data_length.W))
    val PCPlus8D_Reg = RegInit(0.U(data_length.W))
    val PCD_Reg = RegInit(0.U(data_length.W))
    val ExceptionTypeD_Reg = RegInit(0.U(2.W))
    val InDelaySlotD_Reg = RegInit(0.U(1.W))

    // val read_bank_point_Reg = RegInit(0.U(bank_width.W))
    // val read_length_point_Reg = RegInit(0.U(length_width.W))

    io.InstrD := InstrD_Reg 
    io.PCPlus4D :=  PCPlus4D_Reg
    io.PCPlus8D :=  PCPlus8D_Reg
    io.InDelaySlotD :=  InDelaySlotD_Reg
    io.ExceptionTypeD_Out := ExceptionTypeD_Reg
    io.PCD  :=  PCD_Reg
    // io.read_bank_pointD := read_bank_point_Reg
    // io.read_length_pointD := read_length_point_Reg

    InstrD_Reg  :=             Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.InstrF,InstrD_Reg  ))
    PCPlus4D_Reg  :=           Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.PCPlus4F,PCPlus4D_Reg ))
    PCPlus8D_Reg  :=           Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.PCPlus8F, PCPlus8D_Reg ))
    PCD_Reg                 :=     Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.PCF,PCD_Reg ))
    ExceptionTypeD_Reg      :=     Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.ExceptionTypeF, ExceptionTypeD_Reg )) 
    InDelaySlotD_Reg        :=     Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.NextDelaySlotD, InDelaySlotD_Reg ))
    // read_bank_point_Reg     :=     Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.read_bank_pointF, read_bank_point_Reg )) 
    // read_length_point_Reg   :=     Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.read_length_pointF, read_length_point_Reg ))

}

// object if2id_test extends App{
//     (new ChiselStage).emitVerilog(new if2id )
// }

