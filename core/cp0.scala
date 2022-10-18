package examples

import chisel3._
import chisel3.stage._
import chisel3.util._


class cp0 extends Module with riscv_macros {//hi = Input(UInt(32.W))lo寄存器
        //完全没用到chisel真正好的地方，我是废物呜呜呜呜
    val io = IO(new Bundle { 

    val      cp0_read_addr = Input(UInt(5.W))
    val      cp0_read_sel = Input(UInt(3.W))

    val      cp0_write_addr = Input(UInt(5.W))
    val      cp0_write_sel = Input(UInt(3.W))
    val      cp0_write_data = Input(UInt(32.W))
    val      cp0_write_en = Input(UInt(1.W))
    
    val      int_i = Input(UInt(6.W))
    val      timer_int_has = Output(Bool())
    val      pc = Input(UInt(32.W))
    val      mem_bad_vaddr = Input(UInt(32.W))
    val      exception_type_i = Input(UInt(32.W))
    val      in_delayslot = Input(UInt(1.W))
    val      in_branchjump_jr = Input(UInt(2.W))

    val      return_pc = Output(UInt(32.W))
    val      exception = Output(UInt(1.W))
    val      cp0_read_data = Output(UInt(32.W))
    val      cp0_random = Output(UInt(32.W))

    val      epc = Output(UInt(32.W))
    val      cp0_status = Output(UInt(6.W))
    val      Int_able = Output(Bool())
    val      asid    =  Output(UInt(8.W))

    val      cp0_tlb_read_data  = Flipped(new tlb_write_or_read_port)
    val      cp0_tlb_write_data = new tlb_write_or_read_port
    val      cp0_tlb_write_en   = Input(Bool())
    val      cp0_index_tlb_write_able = Input(Bool())
    })
    val ebase_reset_value = "b10_1011_1111_1100_0000_0000_0000_0000_00".U
    //"1.U(1.W),0.U(1.W),0xbfc0.U(16.W),0.U(4.W),0.U(10.W)

    val cp0_index = RegInit(0.U(32.W))    // 0
    val cp0_random = RegInit(0.U(32.W))   // 1
    val cp0_entrylo0 = RegInit(0.U(32.W)) // 2
    val cp0_entrylo1 = RegInit(0.U(32.W)) // 3
    val cp0_pagemask = RegInit(0.U(32.W)) // 5
    val cp0_badvaddr = RegInit(0.U(32.W)) // 8
    val cp0_count = RegInit(0.U(32.W))    // 9
    val cp0_entryhi = RegInit(0.U(32.W))  // 10
    val cp0_compare = RegInit(0.U(32.W))  // 11
    val cp0_status = RegInit("b0000_0000_0100_0000_0000_0000_0000_0000".U(32.W))   // 12
    val cp0_cause = RegInit(0.U(32.W))    // 13
    val cp0_epc = RegInit(0.U(32.W))      // 14
    val cp0_prid = RegInit(0.U(32.W))     // 15 0
    
    val cp0_ebase = RegInit(ebase_reset_value)
    val cp0_config0 = RegInit("b1_000_0000_0000_0000_0_00_000_001_0000_010".U(32.W)) // 16, 0
    val cp0_config1 = "b0_001111_00000_00000_00000_00000_00000".U(32.W)  // 16, 1

    val cp0_counter_half = RegInit(0.U.asBool)
    val cp0_counter_half_last = RegInit(0.U.asBool)
    cp0_counter_half := Mux(clock.asBool,~cp0_counter_half,cp0_counter_half)
    cp0_counter_half_last := cp0_counter_half


    val reset_data_380 = "b1011_1111_1100_0000_0000_0011_1000_0000".U
    val reset_data_200 = "b1011_1111_1100_0000_0000_0010_0000_0000".U
    //Cat(0xbfc0.U(16.W),0.U(4.W),0x380.U(12.W))
    val plus_380_exception_pc = RegInit(reset_data_380)
    plus_380_exception_pc := Mux(cp0_status(22),Cat(0xbfc0.U(16.W),0x0380.U(16.W)),Cat(cp0_ebase(31,12),0x180.U(12.W)))
    val plus_200_exception_pc = RegInit(reset_data_200)
    plus_200_exception_pc := Mux(cp0_status(22),Cat(0xbfc0.U(16.W),0x0200.U(16.W)),Cat(cp0_ebase(31,12),0x0.U(12.W)))

    val pc_Reg = RegInit(0.U(32.W))
    pc_Reg := io.pc

    io.epc := cp0_epc
    io.Int_able := ! cp0_status(1) && cp0_status(0)
    io.cp0_status :=  cp0_status(15,10)
    

    val int_signal  =((cp0_status(15,8) & cp0_cause(15,8)) =/= 0.U) &&  ! cp0_status(1) && cp0_status(0) //感觉这个写法有问题，逻辑太长了.用来判断到底有没有中断
    val exception_type   = Cat(io.exception_type_i(31,1),int_signal)//15-8分别是六根硬件中断线和两根软件中断线

    val exl_Reg     = Wire(UInt(1.W))//status 寄存器第1位 1表示例外级
    val commit_exception =(exception_type(30,0) =/= 0.U) && !exl_Reg//不等和等于运算更加耗时
    val commit_in_delayslot = Mux(int_signal || commit_exception ,io.in_delayslot,cp0_cause(31)) //判断是不是在延迟槽
    val commit_eret  = Mux(exception_type(31) && !exception_type(EXCEP_AdELI),1.U(1.W),0.U(1.W) ) //判断到底是啥例外，如果是eret过程中的地址错乱的话，就不是eret例外
    io.exception     :=  commit_exception || commit_eret.asBool // 有没异常或者是回调的东西
    exl_Reg := cp0_status(1)

    val commit_next_pc   = Mux(int_signal.asBool,Mux(io.in_delayslot.asBool,//int_signal也可以表示是精确例外
   (io.pc - 4.U),Mux(io.in_branchjump_jr =/= 0.U,io.pc,io.pc+4.U)),Mux(io.in_delayslot.asBool,(io.pc - 4.U),io.pc))  // 处于分支延迟槽，记录前一条分支指令，响应非精确例外，写入下一条指令
    val commit_epc   =  Mux(exception_type(EXCEP_AdELI) && exception_type(EXCEP_ERET),cp0_epc,commit_next_pc)
// 写法有待商榷
    val cp0_read_data_Wire = Wire(UInt(32.W))
    val read_addr_sel     = Cat(io.cp0_read_addr,io.cp0_read_sel(0)) //便于进行特判,只用最后一位，以便形成六位的lut
    val write_addr_sel    = Cat(io.cp0_write_addr,io.cp0_write_sel(0))//便于进行特判
    io.cp0_read_data  := Mux(reset.asBool,0.U,cp0_read_data_Wire) 

    val cause_exccode_Wire = Wire(UInt(5.W)) // cause(6,2)
    val commit_bvaddr_Wire = Wire(UInt(32.W))//badvaddr
    val return_pc_Wire     = Wire(UInt(32.W))//RegInit(0.U(32.W))
    io.return_pc := return_pc_Wire
    
    return_pc_Wire  := MuxCase(plus_380_exception_pc,Seq(
        // exception_type(EXCEP_INT) -> Cat(0xbfc0.U(16.W),0x0380.U(16.W)),
        // exception_type(EXCEP_AdELD) -> Cat(0xbfc0.U(16.W),0x0380.U(16.W)),
        // exception_type(EXCEP_AdELI)  -> Cat(0xbfc0.U(16.W),0x0380.U(16.W)),
        // exception_type(EXCEP_AdES) -> Cat(0xbfc0.U(16.W),0x0380.U(16.W)),
        // exception_type(EXCEP_Sys)   -> Cat(0xbfc0.U(16.W),0x0380.U(16.W)),
        // exception_type(EXCEP_Bp) -> Cat(0xbfc0.U(16.W),0x0380.U(16.W)),
        // exception_type(EXCEP_Ov)-> Cat(0xbfc0.U(16.W),0x0380.U(16.W)),
        (exception_type(EXCEP_RI)|| exception_type(EXCEP_TLBRefill_L)|| exception_type(EXCEP_TLBRefill_S) )-> plus_200_exception_pc,//保留指令例外
        (exception_type(EXCEP_ERET) && !exception_type(EXCEP_AdELI))-> cp0_epc // 函数回调
    )) 
    cause_exccode_Wire := Mux1H(Seq(exception_type(EXCEP_INT)-> EXCEP_CODE_INT, //中断
        (exception_type(EXCEP_AdELD) || exception_type(EXCEP_AdELI)) -> EXCEP_CODE_AdEL, //指令地址错误或者数据地址错误'
        exception_type(EXCEP_AdES) -> EXCEP_CODE_AdES,exception_type(EXCEP_Sys) -> EXCEP_CODE_Sys,
        exception_type(EXCEP_Bp)   -> EXCEP_CODE_Bp  ,exception_type(EXCEP_RI)  -> EXCEP_CODE_RI,
        exception_type(EXCEP_CODE_Ov) -> EXCEP_CODE_Ov ,exception_type(EXCEP_TLBRefill_L) -> EXCEP_CODE_TLBL,
        exception_type(EXCEP_TLBRefill_S) -> EXCEP_CODE_TLBS ,exception_type(EXCEP_TLBInvalid_L) -> EXCEP_CODE_TLBL,
        exception_type(EXCEP_TLBInvalid_S) -> EXCEP_CODE_TLBS ,exception_type(EXCEP_TLBModified)  -> EXCEP_CODE_MOD
    ))
    commit_bvaddr_Wire := Mux(exception_type(EXCEP_AdELI) && !exception_type(EXCEP_ERET),io.pc,io.mem_bad_vaddr)
    val write_and_read_same = (write_addr_sel === read_addr_sel) && io.cp0_write_en.asBool

//read 不能用寄存器,随时出结果,随时取
    cp0_read_data_Wire := MuxLookup(read_addr_sel,0.U,Seq(
        CP0_ADDR_SEL_INDEX    -> Cat(cp0_index(31),0.U(27.W),Mux(write_and_read_same,io.cp0_write_data(3,0),cp0_index(3,0))),//Mux(write_and_read_same,io.cp0_write_data,cp0_index),
        CP0_ADDR_SEL_RANDOM   -> cp0_random,//手册没看到，应该得在完整手册里面
        CP0_ADDR_SEL_ENTRYLO0 -> Cat(0.U(6.W),Mux(write_and_read_same, io.cp0_write_data(25,0), cp0_entrylo0(25,0))),
        CP0_ADDR_SEL_ENTRYLO1 -> Cat(0.U(6.W),Mux(write_and_read_same, io.cp0_write_data(25,0), cp0_entrylo1(25,0))),
        CP0_ADDR_SEL_PAGEMASK -> Cat(0.U(7.W),Mux(write_and_read_same, io.cp0_write_data(24,13), 0.U(13.W))),
        CP0_ADDR_SEL_BADVADDR -> cp0_badvaddr,
        CP0_ADDR_SEL_COUNT    -> Mux(write_and_read_same, io.cp0_write_data,cp0_count),
        CP0_ADDR_SEL_ENTRYHI  -> Cat(Mux(write_and_read_same, io.cp0_write_data(31,13), cp0_entryhi(31,13)),0.U(5.W),
                                 Mux(write_and_read_same, io.cp0_write_data(7,0), cp0_entryhi(7,0))),
        CP0_ADDR_SEL_COMPARE  -> Mux(write_and_read_same, io.cp0_write_data, cp0_compare),
        CP0_ADDR_SEL_STATUS   -> Mux(write_and_read_same,Cat(io.cp0_write_data(31,24),0.U(1.W),io.cp0_write_data(22,8),0.U(3.W),io.cp0_write_data(4,0)),cp0_status),
        //  Cat("b000000000_1_000000".U(16.W), Mux(write_and_read_same, io.cp0_write_data(15,8), cp0_status(15,8)), 0.U(6.W),
        //                          Mux(write_and_read_same, io.cp0_write_data(1,0), cp0_status(1,0))),
        CP0_ADDR_SEL_CAUSE    -> Cat(cp0_cause(31,10),Mux(write_and_read_same, io.cp0_write_data(9,8),cp0_cause(9,8)),cp0_cause(7,0)),
        CP0_ADDR_SEL_EPC      -> Mux(write_and_read_same, io.cp0_write_data, cp0_epc),
        CP0_ADDR_SEL_PRID     -> cp0_prid, //手册没看到，应该得在完整手册里面
        CP0_ADDR_SEL_CONFIG0  -> Cat(cp0_config0(31,3),Mux(write_and_read_same, io.cp0_write_data(2,0),cp0_config0(2,0))),
        CP0_ADDR_SEL_CONFIG1  -> cp0_config1,  //config 只读,
        CP0_ADDR_SEL_EBASE    -> Cat(cp0_ebase(31,30),Mux(write_and_read_same, io.cp0_write_data(29,12),cp0_ebase(29,12)),cp0_ebase(11,0))
    ))
    //write
    val cp0_write_able = io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_INDEX

    cp0_index := Cat(Mux(io.cp0_index_tlb_write_able,io.cp0_write_data(31),cp0_index(31)),0.U(27.W),Mux(cp0_write_able,io.cp0_write_data(3,0),cp0_index(3,0)))
    cp0_random := cp0_random(4,0) + 1.U
    cp0_entrylo0 := Mux(io.cp0_tlb_write_en.asBool,Cat(0.U(6.W),io.cp0_tlb_write_data.paddr(0),io.cp0_tlb_write_data.c(0),io.cp0_tlb_write_data.d(0),
        io.cp0_tlb_write_data.v(0),io.cp0_tlb_write_data.g),Mux(io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_ENTRYLO0,io.cp0_write_data(25,0),cp0_entrylo0(25,0)))
    cp0_entrylo1 := Mux(io.cp0_tlb_write_en.asBool,Cat(0.U(6.W),io.cp0_tlb_write_data.paddr(1),io.cp0_tlb_write_data.c(1),io.cp0_tlb_write_data.d(1),
        io.cp0_tlb_write_data.v(1),io.cp0_tlb_write_data.g),Mux(io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_ENTRYLO1,io.cp0_write_data(25,0),cp0_entrylo1(25,0)))
    cp0_pagemask := Cat(Mux(io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_PAGEMASK,io.cp0_write_data(24,13),cp0_pagemask(24,13)),0.U(13.W))
    cp0_badvaddr       := Mux(commit_exception,commit_bvaddr_Wire,cp0_badvaddr )
    cp0_count          := Mux(io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_COUNT,io.cp0_write_data, Mux(cp0_counter_half ,(cp0_count + 1.U),cp0_count))
    cp0_entryhi := Mux(io.cp0_tlb_write_en,Cat(io.cp0_tlb_write_data.vaddr,0.U(5.W),io.cp0_tlb_write_data.asid),
                        Cat(Mux(io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_ENTRYHI,io.cp0_write_data(31,13),cp0_entryhi(31,13)),0.U(5.W),
                        Mux(io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_ENTRYHI,io.cp0_write_data(7,0),cp0_entryhi(7,0))))
    cp0_compare :=  Mux(io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_COMPARE,io.cp0_write_data,cp0_compare)        
    cp0_status  :=   MuxCase(cp0_status,Seq(
        (commit_exception || commit_eret.asBool) -> Cat(cp0_status(31,2),commit_exception,cp0_status(0)), //发生例外时置 1,感觉这里有问题
        (io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_STATUS) -> Cat(io.cp0_write_data(31,24),0.U(1.W),io.cp0_write_data(22,8),0.U(3.W),io.cp0_write_data(4,0))
   ))
    cp0_ebase   :=  Cat(cp0_ebase(31,30),Mux(io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_EBASE,io.cp0_write_data(29,12),cp0_ebase(29,12)),cp0_ebase(11,0))
    
    //Mux(io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_COMPARE,io.cp0_write_data,cp0_compare) 
   val compare_write = io.cp0_write_en.asBool && (write_addr_sel === CP0_ADDR_SEL_COMPARE)
   val cause_write_en = io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_CAUSE // (9,8)为软件中断，外部写入
   val timer_int   =  Mux(cp0_compare =/= 0.U && cp0_count === cp0_compare &&(!compare_write)  ,1.U(1.W),Mux(compare_write,0.U(1.W),cp0_cause(30))) //定时器中断,写了compare要将这一位清零  
   io.timer_int_has := cp0_compare =/= 0.U && cp0_count === cp0_compare//timer_int
   val interrupt   =  io.int_i(5,0) //代表是哪一根中断线

   cp0_cause   := Cat(  commit_in_delayslot,timer_int,0.U(14.W) , interrupt , Mux(cause_write_en,io.cp0_write_data(9,8),cp0_cause(9,8)),
                        0.U(1.W), Mux(exl_Reg.asBool,cp0_cause(6,2),cause_exccode_Wire),0.U(2.W) )
   cp0_epc  := MuxCase(cp0_epc,Seq(
       commit_exception.asBool -> commit_epc,
       (io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_EPC) -> io.cp0_write_data
   ))
   cp0_config0  := Cat(cp0_config0(31,3),Mux((io.cp0_write_en.asBool && write_addr_sel === CP0_ADDR_SEL_CONFIG0),io.cp0_write_data(2,0),cp0_config0(2,0)))

   io.cp0_tlb_read_data.vaddr    :=  cp0_entryhi(31,13)
   io.cp0_tlb_read_data.asid     :=  cp0_entryhi(7,0)
   io.cp0_tlb_read_data.g        :=  cp0_entrylo0(0) && cp0_entrylo1(0)
   io.cp0_tlb_read_data.paddr(0) :=  cp0_entrylo0(25,6)
   io.cp0_tlb_read_data.paddr(1) :=  cp0_entrylo1(25,6)
   io.cp0_tlb_read_data.c(0)     :=  cp0_entrylo0(5,3)
   io.cp0_tlb_read_data.c(1)     :=  cp0_entrylo1(5,3)
   io.cp0_tlb_read_data.d(0)     :=  cp0_entrylo0(2)
   io.cp0_tlb_read_data.d(1)     :=  cp0_entrylo1(2)
   io.cp0_tlb_read_data.v(0)     :=  cp0_entrylo0(1)
   io.cp0_tlb_read_data.v(1)     :=  cp0_entrylo1(1)   

   io.asid := cp0_entryhi(7,0)
   io.cp0_random := cp0_random

}

// object cp0_test extends App{
//     (new ChiselStage).emitVerilog(new cp0)
// }

