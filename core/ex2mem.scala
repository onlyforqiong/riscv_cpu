package examples

import chisel3._
import chisel3.stage._
import chisel3.util._
import dataclass.data


class ex2mem extends Module with riscv_macros {//
        //完全没用到chisel真正好的地方，我是废物呜呜呜呜\
    val io1 = (IO(Flipped(new ex_in_and_out_port)))
    val io = IO(new Bundle {
        val    en   = Input(UInt(1.W))
        val    clr   = Input(UInt(1.W))
        
        // val    RegWriteE   = Input(UInt(1.W))
        // val    MemToRegE   = Input(UInt(1.W))
        // val    MemWriteE   = Input(UInt(1.W))
        val    ALUOutE   = Input(UInt(data_length.W))
        val    WriteDataE   = Input(UInt(data_length.W))
        val    WriteRegE   = Input(UInt(5.W))
        // val    LoadUnsignedE   = Input(UInt(1.W))
        // val    MemWidthE   = Input(UInt(2.W))
        val    PhyAddrE   = Input(UInt(data_length.W))
        // val    LinkE   = Input(UInt(1.W))
        // val    PCPlus4E   = Input(UInt(32.W))
        // val    HiLoWriteE   = Input(UInt(2.W))
        // val    HiLoToRegE   = Input(UInt(2.W))
        val    HiLoOutE   = Input(UInt(32.W))
        val    HiInE   = Input(UInt(32.W))
        val    LoInE   = Input(UInt(32.W))
        val    CP0WriteE   = Input(UInt(1.W))
        val    CP0ToRegE   = Input(UInt(1.W))
        // val    WriteCP0AddrE   = Input(UInt(5.W))
        // val    WriteCP0SelE   = Input(UInt(3.W))
        val    WriteCP0HiLoDataE   = Input(UInt(32.W))
        val    ReadCP0DataE   = Input(UInt(32.W))
        // val    PCE   = Input(UInt(32.W))
        // val    InDelaySlotE   = Input(UInt(1.W))
        val    BadVAddrE   = Input(UInt(data_length.W))
        val    ExceptionTypeE   = Input(UInt(32.W))
        val    RtE         = Input(UInt(data_length.W))
        val    Pc_NextE     = Input(UInt(data_length.W))
        val   mem_trace_budleE = Input(new mtrace_relative_bundle)
        
        val   RegWriteM   = Output(UInt(1.W))
        val   MemToRegM   = Output(UInt(1.W))
        val   MemWriteM   = Output(UInt(1.W))
        val   ALUOutM   = Output(UInt(data_length.W))
        val   WriteDataM   = Output(UInt(data_length.W))
        val   WriteRegM   = Output(UInt(5.W))
        val   LinkM   = Output(UInt(1.W))
        val   PCPlus4M   = Output(UInt(data_length.W))
        val   LoadUnsignedM   = Output(UInt(1.W))
        val   MemWidthM   = Output(UInt(2.W))
        val   PhyAddrM   = Output(UInt(data_length.W))
        val   HiLoWriteM   = Output(UInt(2.W))
        val   HiLoToRegM   = Output(UInt(2.W))
        val   HiLoOutM   = Output(UInt(32.W))
        val   HiInM   = Output(UInt(32.W))
        val   LoInM   = Output(UInt(32.W))
        val   CP0WriteM   = Output(UInt(1.W))
        val   CP0ToRegM   = Output(UInt(1.W))
        val   WriteCP0AddrM   = Output(UInt(5.W))
        val   WriteCP0SelM   = Output(UInt(3.W))
        val   WriteCP0HiLoDataM   = Output(UInt(32.W))
        val   ReadCP0DataM   = Output(UInt(32.W))
        val   PCM   = Output(UInt(data_length.W))
        val   InDelaySlotM   = Output(UInt(1.W))
        val   BadVAddrM   = Output(UInt(data_length.W))
        val   ExceptionTypeM_Out = Output(UInt(32.W))
        val   MemRLM      = Output(UInt(2.W))
        val   RtM         = Output(UInt(data_length.W))
        val   BranchJump_JrM = Output(UInt(2.W))
        val   Tlb_ControlM   = Output(UInt(3.W))
        val   eBreakM        = Output(Bool())
        val   Pc_NextM       = Output(UInt(data_length.W))
        val   mem_trace_budleM = Output(new mtrace_relative_bundle)
    })
        val   RegWrite_Reg  = RegInit(0.U(1.W))
        val   MemToReg_Reg  = RegInit(0.U(1.W))
        val   MemWrite_Reg  = RegInit(0.U(1.W))
        val   ALUOut_Reg  = RegInit(0.U(data_length.W))
        val   WriteData_Reg  = RegInit(0.U(data_length.W))
        val   WriteReg_Reg  = RegInit(0.U(5.W))
        val   Link_Reg  = RegInit(0.U(1.W))
        val   PCPlus4_Reg  = RegInit(0.U(data_length.W))
        val   LoadUnsigned_Reg  = RegInit(0.U(1.W))
        val   MemWidth_Reg  = RegInit(0.U(2.W))
        val   PhyAddr_Reg  = RegInit(0.U(data_length.W))
        val   HiLoWrite_Reg  = RegInit(0.U(2.W))
        val   HiLoToReg_Reg  = RegInit(0.U(2.W))
        val   HiLoOut_Reg  = RegInit(0.U(32.W))
        val   HiIn_Reg  = RegInit(0.U(32.W))
        val   LoIn_Reg  = RegInit(0.U(32.W))
        val   CP0Write_Reg  = RegInit(0.U(1.W))
        val   CP0ToReg_Reg  = RegInit(0.U(1.W))
        val   WriteCP0Addr_Reg  = RegInit(0.U(5.W))
        val   WriteCP0Sel_Reg  = RegInit(0.U(3.W))
        val   WriteCP0HiLoData_Reg  = RegInit(0.U(32.W))
        val   ReadCP0Data_Reg  = RegInit(0.U(32.W))
        val   PC_Reg  = RegInit(0.U(data_length.W))
        val   InDelaySlot_Reg  = RegInit(0.U(1.W))
        val   BadVAddr_Reg  = RegInit(0.U(data_length.W))
        val   ExceptionType_Reg= RegInit(0.U(32.W))
        val   MemRLM_Reg      = RegInit(0.U(2.W))
        val   RtM_Reg = RegInit(0.U(data_length.W))
        val   BranchJump_JrM_Reg = RegInit(0.U(2.W))
        val   Tlb_Control_Reg   =  RegInit(0.U(3.W))
        val   eBreak_Reg  = RegInit(0.U.asBool())
        val   pc_nextReg  = RegInit(0.U(data_length.W))
        val   mem_trace_budleReg = RegInit(0.U.asTypeOf(new mtrace_relative_bundle))
  
        RegWrite_Reg:=            Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.RegWriteE ,  RegWrite_Reg))
        MemWrite_Reg:=            Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.MemWriteE,MemWrite_Reg))
        ALUOut_Reg:=              Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.ALUOutE,ALUOut_Reg))
        WriteData_Reg:=           Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.WriteDataE,WriteData_Reg))
        WriteReg_Reg:=            Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.WriteRegE,WriteReg_Reg))
        Link_Reg:=                Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.LinkE,Link_Reg))
        PCPlus4_Reg:=             Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.PCPlus4E,PCPlus4_Reg))
        LoadUnsigned_Reg:=        Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.LoadUnsignedE,LoadUnsigned_Reg))
        MemWidth_Reg:=            Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.MemWidthE,MemWidth_Reg))
        PhyAddr_Reg:=             Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.PhyAddrE,PhyAddr_Reg))
        HiLoWrite_Reg:=           Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.HiLoWriteE, HiLoWrite_Reg))
        HiLoToReg_Reg:=           Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.HiLoToRegE,HiLoToReg_Reg))
        HiLoOut_Reg:=             Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.HiLoOutE,HiLoOut_Reg))
        HiIn_Reg:=                Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.HiInE,HiIn_Reg))
        LoIn_Reg:=                Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.LoInE, LoIn_Reg))
        CP0Write_Reg:=            Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.CP0WriteE,CP0Write_Reg))
        CP0ToReg_Reg:=            Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.CP0ToRegE,CP0ToReg_Reg))
        WriteCP0Addr_Reg:=        Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.WriteCP0AddrE, WriteCP0Addr_Reg))
        WriteCP0Sel_Reg:=         Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.WriteCP0SelE,WriteCP0Sel_Reg))
        WriteCP0HiLoData_Reg:=    Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.WriteCP0HiLoDataE,WriteCP0HiLoData_Reg))
        ReadCP0Data_Reg:=         Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.ReadCP0DataE, ReadCP0Data_Reg))
        PC_Reg:=                  Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.PCE,PC_Reg))
        InDelaySlot_Reg:=         Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.InDelaySlotE,InDelaySlot_Reg))
        BadVAddr_Reg:=            Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.BadVAddrE, BadVAddr_Reg))
        ExceptionType_Reg    :=   Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io.ExceptionTypeE, ExceptionType_Reg))
        MemToReg_Reg         :=   Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.MemToRegE, MemToReg_Reg ))
        MemRLM_Reg           :=   Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.MemRLE, MemRLM_Reg ))
        RtM_Reg              :=   Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.RtE,RtM_Reg))
        BranchJump_JrM_Reg   :=   Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io1.BranchJump_JrE, BranchJump_JrM_Reg))
        Tlb_Control_Reg      :=   Mux(io.clr.asBool,0.U, Mux(io.en.asBool,io1.Tlb_Control, Tlb_Control_Reg))
        eBreak_Reg           :=   Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io1.eBreakE,eBreak_Reg))
        pc_nextReg           :=   Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.Pc_NextE,pc_nextReg)) 
        mem_trace_budleReg   :=   Mux(io.clr.asBool,0.U.asTypeOf(new mtrace_relative_bundle),Mux(io.en.asBool,io.mem_trace_budleE,mem_trace_budleReg))
       
        io.RegWriteM         := RegWrite_Reg
        io.MemToRegM         := MemToReg_Reg
        io.MemWriteM         := MemWrite_Reg
        io.ALUOutM           := ALUOut_Reg
        io.WriteDataM        := WriteData_Reg
        io.WriteRegM         := WriteReg_Reg
        io.LinkM             := Link_Reg
        io.PCPlus4M          := PCPlus4_Reg
        io.LoadUnsignedM     := LoadUnsigned_Reg
        io.MemWidthM         := MemWidth_Reg
        io.PhyAddrM          := PhyAddr_Reg
        io.HiLoWriteM        := HiLoWrite_Reg
        io.HiLoToRegM        := HiLoToReg_Reg
        io.HiLoOutM          := HiLoOut_Reg
        io.HiInM             := HiIn_Reg
        io.LoInM             := LoIn_Reg
        io.CP0WriteM         := CP0Write_Reg
        io.CP0ToRegM         := CP0ToReg_Reg
        io.WriteCP0AddrM     := WriteCP0Addr_Reg
        io.WriteCP0SelM      := WriteCP0Sel_Reg
        io.WriteCP0HiLoDataM := WriteCP0HiLoData_Reg
        io.ReadCP0DataM      := ReadCP0Data_Reg
        io.PCM               := PC_Reg
        io.InDelaySlotM      := InDelaySlot_Reg
        io.BadVAddrM         := BadVAddr_Reg
        io.ExceptionTypeM_Out:= ExceptionType_Reg
        io.MemRLM            := MemRLM_Reg 
        io.RtM               := RtM_Reg 
        io.BranchJump_JrM    := BranchJump_JrM_Reg 
        io.Tlb_ControlM      := Tlb_Control_Reg
        io.eBreakM           := eBreak_Reg
        io.Pc_NextM          := pc_nextReg
        io.mem_trace_budleM   := mem_trace_budleReg


}
// object ex2mem_test extends App{
//     (new ChiselStage).emitVerilog(new ex2mem)
// }

