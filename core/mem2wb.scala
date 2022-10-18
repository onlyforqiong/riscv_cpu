package examples

import chisel3._
import chisel3.stage._
import chisel3.util._


class mem2wb extends Module with riscv_macros {//
        //完全没用到chisel真正好的地方，我是废物呜呜呜呜
    val io = IO(new Bundle {
        val        en = Input(UInt(1.W))
        val        clr = Input(UInt(1.W))
        
        val   RegWriteM = Input(UInt(1.W))
        val   MemToRegM = Input(UInt(1.W))
        val   ReadDataM = Input(UInt(data_length.W))
        val   ResultM = Input(UInt(data_length.W))
        val   WriteRegM = Input(UInt(5.W))
        val   HiLoWriteM = Input(UInt(2.W))
        val   HiInM = Input(UInt(data_length.W))
        val   LoInM = Input(UInt(data_length.W))
        val   CP0WriteM = Input(UInt(1.W))
        val   WriteCP0AddrM = Input(UInt(5.W))
        val   WriteCP0SelM = Input(UInt(3.W))
        val   WriteCP0HiLoDataM = Input(UInt(data_length.W))
        val   PCM = Input(UInt(data_length.W))
        val   InDelaySlotM = Input(UInt(1.W))
        val   BadVAddrM = Input(UInt(data_length.W))
        val   ExceptionTypeM = Input(UInt(32.W))
        val   BranchJump_JrM = Input(UInt(2.W))
        val   Tlb_ControlM    = Input(UInt(3.W))
        val   eBreakM         = Input(Bool())
        val   Pc_NextM        = Input(UInt(data_length.W))
        val   Mem_trace_budleM = Input(new mtrace_relative_bundle)
        // val   MemToRegM_Forward_hasStall = Input(UInt(1.W))
        // val   MemReadDataM = Input(UInt(32.W))
        
        val   RegWriteW_Out = Output(UInt(1.W))
        val   MemToRegW = Output(UInt(1.W))
        val   ReadDataW = Output(UInt(data_length.W))
        val   ResultW = Output(UInt(data_length.W))
        val   WriteRegW = Output(UInt(5.W))
        val   HiLoWriteW = Output(UInt(2.W))
        val   HiInW = Output(UInt(data_length.W))
        val   LoInW = Output(UInt(data_length.W))
        val   CP0WriteW = Output(UInt(1.W))
        val   WriteCP0AddrW = Output(UInt(5.W))
        val   WriteCP0SelW = Output(UInt(3.W))
        val   WriteCP0HiLoDataW = Output(UInt(data_length.W))
        val   PCW = Output(UInt(data_length.W))
        val   InDelaySlotW = Output(UInt(1.W))
        val   BadVAddrW = Output(UInt(data_length.W))
        val   ExceptionTypeW_Out = Output(UInt(32.W))
        val   BranchJump_JrW = Output(UInt(2.W))
        val   Tlb_ControlW   = Output(UInt(3.W))
        val   eBreakW        = Output(Bool())
        val   Pc_NextW       = Output(UInt(data_length.W))
        val   Mem_trace_budleW = Output(new mtrace_relative_bundle)
        // val   MemToRegW_Forward_hasStall WOutput(UInt(1.W)) 
        // val   MemReadDataW = Output(UInt(32.W))
    })
            
        val   RegWriteW = RegInit(0.U(1.W))
        val   MemToRegW = RegInit(0.U(1.W))
        val   ReadDataW = RegInit(0.U(data_length.W))
        val   ResultW = RegInit(0.U(data_length.W))
        val   WriteRegW = RegInit(0.U(5.W))
        val   HiLoWriteW = RegInit(0.U(2.W))
        val   HiInW = RegInit(0.U(data_length.W))
        val   LoInW = RegInit(0.U(data_length.W))
        val   CP0WriteW = RegInit(0.U(1.W))
        val   WriteCP0AddrW = RegInit(0.U(5.W))
        val   WriteCP0SelW = RegInit(0.U(3.W))
        val   WriteCP0HiLoDataW = RegInit(0.U(data_length.W))
        val   PCW = RegInit(0.U(data_length.W))
        val   InDelaySlotW = RegInit(0.U(1.W))
        val   BadVAddrW = RegInit(0.U(data_length.W))
        val   ExceptionTypeW = RegInit(0.U(32.W))
        
        val   BranchJump_JrW_Reg = RegInit(0.U(2.W))
        val   Tlb_Control_Reg = RegInit(0.U(3.W))
        val   ebreak_Reg      = RegInit(0.U.asBool)
        val   pc_nextReg  = RegInit(0.U(data_length.W))
        val   Mem_trace_budleReg = RegInit(0.U.asTypeOf(new mtrace_relative_bundle))
        // val   MemToRegW_Forward_hasStall_Reg =RegInit(0.U(1.W))
        // val   MemReadDataW_Reg = RegInit(0.U(32.W))
        io.RegWriteW_Out           := RegWriteW
        io.MemToRegW           := MemToRegW
        io.ReadDataW           := ReadDataW
        io.ResultW             := ResultW
        io.WriteRegW           := WriteRegW
        io.HiLoWriteW          := HiLoWriteW
        io.HiInW               := HiInW
        io.LoInW               := LoInW
        io.CP0WriteW           := CP0WriteW
        io.WriteCP0AddrW       := WriteCP0AddrW
        io.WriteCP0SelW        := WriteCP0SelW
        io.WriteCP0HiLoDataW   := WriteCP0HiLoDataW
        io.PCW                 := PCW
        io.InDelaySlotW        := InDelaySlotW
        io.BadVAddrW           := BadVAddrW
        io.ExceptionTypeW_Out  := ExceptionTypeW
        io.BranchJump_JrW      := BranchJump_JrW_Reg   
        io.Tlb_ControlW        := Tlb_Control_Reg
        io.eBreakW             := ebreak_Reg
        io.Pc_NextW            := pc_nextReg
        io.Mem_trace_budleW     := Mem_trace_budleReg
        // io.MemToRegW_Forward_hasStall := MemToRegW_Forward_hasStall_Reg
        // io.MemReadDataW        := MemReadDataW_Reg     
       

        RegWriteW          := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. RegWriteM,RegWriteW))
        MemToRegW          := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. MemToRegM,MemToRegW))
        ReadDataW          := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. ReadDataM,ReadDataW))
        ResultW            := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. ResultM,ResultW))
        WriteRegW          := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. WriteRegM,WriteRegW))
        HiLoWriteW         := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. HiLoWriteM,HiLoWriteW))
        HiInW              := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. HiInM,HiInW))
        LoInW              := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. LoInM,LoInW))
        CP0WriteW          := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. CP0WriteM,CP0WriteW))
        WriteCP0AddrW      := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. WriteCP0AddrM,WriteCP0AddrW))
        WriteCP0SelW       := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. WriteCP0SelM,WriteCP0SelW))
        WriteCP0HiLoDataW  := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. WriteCP0HiLoDataM,WriteCP0HiLoDataW))
        PCW                := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. PCM,PCW))
        InDelaySlotW       := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. InDelaySlotM,InDelaySlotW))
        BadVAddrW          := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. BadVAddrM,BadVAddrW))
        ExceptionTypeW     := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. ExceptionTypeM,ExceptionTypeW))
        BranchJump_JrW_Reg         :=        Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.BranchJump_JrM, BranchJump_JrW_Reg))
        Tlb_Control_Reg    := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. Tlb_ControlM,Tlb_Control_Reg))
        ebreak_Reg         := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.eBreakM,ebreak_Reg))
        pc_nextReg         := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.Pc_NextM,pc_nextReg))
        Mem_trace_budleReg := Mux(io.clr.asBool,0.U.asTypeOf(new mtrace_relative_bundle),Mux(io.en.asBool,io.Mem_trace_budleM,Mem_trace_budleReg))
        // MemToRegW_Forward_hasStall_Reg         :=        Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io.MemToRegM_Forward_hasStall, MemToRegW_Forward_hasStall_Reg))
        // MemReadDataW_Reg    := Mux(io.clr.asBool,0.U,Mux(io.en.asBool,io. MemReadDataM,MemReadDataW_Reg))
    
}

// object mem2wb_test extends App{
//     (new ChiselStage).emitVerilog(new mem2wb)
// }

