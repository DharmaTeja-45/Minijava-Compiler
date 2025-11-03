package visitor;

import syntaxtree.*;

import java.util.*;

public class liveanalysis<R, A> implements GJVisitor<R, A> {

    Integer line_counter = 1;
    Integer arg = 0;
    blockinfo current_block = new blockinfo(1);
    functioninfo current_function = new functioninfo("main");
    public Map<String, functioninfo> function_map = new HashMap<String, functioninfo>();

    ArrayList<String> registers = new ArrayList<String>(Arrays.asList(
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9"
    ));

    Integer free_reg = 18;
    Map<String, Integer> label_linemap = new HashMap<String, Integer>();
    boolean label_status = false;
    Integer argcounter = 0;

    public void calculate_jumps() {
        for (blockinfo block : current_function.blocks) {
            if (!block.jump_block_name.equals("")) {
                block.jump_block = label_linemap.get(block.jump_block_name);
            }
        }
    }

    public void calculate_inout(functioninfo func) {
        boolean changed = true;

        blockinfo firstBlock = func.blocks.get(0);

        while (changed) {
            changed = false;

            for (int i = current_function.blocks.size() - 1; i >= 0; i--) {
                blockinfo b = current_function.blocks.get(i);

                Set<String> oldIn = new HashSet<String>(b.in);
                Set<String> oldOut = new HashSet<String>(b.out);

                Set<String> outMinusDef = new HashSet<String>(b.out);
                outMinusDef.removeAll(b.def);

                b.in.clear();
                b.in.addAll(outMinusDef);
                b.in.addAll(b.use);

                Integer jump = b.jump_block;
                Integer next = b.next_block;

                blockinfo jumpBlock = new blockinfo(jump);
                blockinfo nextBlock = new blockinfo(next);

                if (next != -1) nextBlock = func.blocks.get(next - firstBlock.line_number);
                if (jump != -1) jumpBlock = func.blocks.get(jump - firstBlock.line_number);

                b.out.clear();
                if (next != -1) b.out.addAll(nextBlock.in);
                if (jump != -1) b.out.addAll(jumpBlock.in);

                if (!oldOut.equals(b.out) || !oldIn.equals(b.in)) changed = true;
            }
        }
    }

    public void calculate_liverange(functioninfo func) {
        for (blockinfo b : func.blocks) {
            Integer blockLine = b.line_number;

            for (String var : b.in) {
                intervalinfo interval = func.interval_map.get(var);
                if (interval == null) {
                    interval = new intervalinfo(var, blockLine, blockLine);
                    func.interval_map.put(var, interval);
                } else {
                    interval.start_line = Math.min(interval.start_line, blockLine);
                    interval.end_line = Math.max(interval.end_line, blockLine);
                }
            }

            for (String var : b.out) {
                intervalinfo interval = func.interval_map.get(var);
                if (interval == null) {
                    interval = new intervalinfo(var, blockLine, blockLine);
                    func.interval_map.put(var, interval);
                } else {
                    interval.start_line = Math.min(interval.start_line, blockLine);
                    interval.end_line = Math.max(interval.end_line, blockLine);
                }
            }
        }
    }

    public String get_free_register(functioninfo func) {
        String result = "-1";
        if (registers.size() > 0) {
            result = registers.get(0);
            registers.remove(result);
        }
        return result;
    }
    

    public void expire(intervalinfo interval, functioninfo func) {
        Collections.sort(func.activelist, Comparator.comparingInt(a -> a.end_line));
        ArrayList<intervalinfo> toRemove = new ArrayList<intervalinfo>();
        for (intervalinfo active : func.activelist) {
            if (active.end_line < interval.start_line) {
                toRemove.add(active);
            }
        }

        for (intervalinfo rem : toRemove) {
            func.activelist.remove(rem);
            if (!rem.allocated_register.equals("-1")) {
                registers.add(rem.allocated_register);
            }
        }
    }

    public void spill(intervalinfo interval, functioninfo func) {
        Integer nActive = func.activelist.size();
        intervalinfo lastActive = func.activelist.get(nActive - 1);

        if (lastActive.end_line > interval.end_line) {
            interval.allocated_register = lastActive.allocated_register;

            func.activelist.remove(lastActive);
            lastActive.allocated_register = "-1";
            func.activelist.add(interval);
            Collections.sort(func.activelist, Comparator.comparingInt(a -> a.end_line));

            lastActive.stack_location = func.stack_ptr;
            func.stack_ptr++;
            func.stack_space.add(lastActive);
        } else {
            interval.stack_location = func.stack_ptr;
            func.stack_ptr++;
            func.stack_space.add(interval);
        }
    }

    public void linear_scan(functioninfo func) {
        for (Map.Entry<String, intervalinfo> entry : func.interval_map.entrySet()) {
            func.intervallist.add(entry.getValue());
        }

        Collections.sort(func.intervallist, Comparator.comparingInt(a -> a.start_line));

        for (intervalinfo interval : func.intervallist) {
            expire(interval, func);

            if (func.activelist.size() == free_reg) {
                func.spilled_check = true;
                spill(interval, func);
            } else {
                String _reg = "-1";
                if (registers.size() > 0) {
                    _reg = registers.get(0);
                    registers.remove(_reg);
                }
                interval.allocated_register = _reg;
                func.activelist.add(interval);
                Collections.sort(func.activelist, Comparator.comparingInt(a -> a.end_line));
            }
        }
    }

    public void allocation() {
        for (Map.Entry<String, functioninfo> entry : function_map.entrySet()) {
            current_function = entry.getValue();
            calculate_jumps();

            blockinfo lastblock = current_function.blocks.get(current_function.blocks.size() - 1);
            lastblock.next_block = -1;
            lastblock.jump_block = -1;

            blockinfo secondLast = current_function.blocks.get(current_function.blocks.size() - 2);
            secondLast.jump_block = -1;
            secondLast.next_block = -1;

            calculate_inout(current_function);
            calculate_liverange(current_function);
            linear_scan(current_function);
            current_function.stack_ptr += Math.max(0, current_function.num_args - 4);
            current_function.total_stack_size = current_function.stack_ptr;
            if (!current_function.function_name.equals("MAIN")) {
                current_function.total_stack_size += 8;
            }
            if (current_function.call_check) {
                current_function.total_stack_size += 10;
            }

            registers = new ArrayList<String>(Arrays.asList(
                    "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                    "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9"
            ));
        }
    }

    public R visit(NodeList n, A argu) {
        R _ret = null;
        int _count = 0;
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, argu);
            _count++;
        }
        return _ret;
    }

    public R visit(NodeListOptional n, A argu) {
        if (n.present()) {
            R _ret = null;
            int _count = 0;
            for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
                e.nextElement().accept(this, argu);
                _count++;
            }
            return _ret;
        } else
            return null;
    }

    public R visit(NodeOptional n, A argu) {
        if (n.present())
            return n.node.accept(this, argu);
        else
            return null;
    }

    public R visit(NodeSequence n, A argu) {
        R _ret = null;
        int _count = 0;
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, argu);
            _count++;
        }
        return _ret;
    }

    public R visit(NodeToken n, A argu) {
        return null;
    }

    public R visit(Goal n, A argu) {
        R _ret = null;

        current_function = new functioninfo("MAIN");
        function_map.put("MAIN", current_function);

        current_block = new blockinfo(line_counter++);
        current_function.blocks.add(current_block);

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);

        current_block = new blockinfo(line_counter++);
        current_function.blocks.add(current_block);

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        allocation();
        return _ret;
    }

    public R visit(StmtList n, A argu) {
        R _ret = null;
        n.f0.accept(this, (A) "label");
        return _ret;
    }

    public R visit(Procedure n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);

        String func_name = (n.f0).f0.tokenImage;
        current_function = new functioninfo(func_name);
        current_function.num_args = (Integer) n.f2.accept(this, argu);
        function_map.put(func_name, current_function);

        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);

        current_block = new blockinfo(line_counter++);
        current_function.blocks.add(current_block);

        n.f4.accept(this, argu);
        return _ret;
    }

    public R visit(Stmt n, A argu) {
        R _ret = null;
        current_block = new blockinfo(line_counter++);
        current_block.line_number = line_counter - 1;
        current_function.blocks.add(current_block);
        n.f0.accept(this, argu);
        return _ret;
    }

    public R visit(NoOpStmt n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);
        return _ret;
    }

    public R visit(ErrorStmt n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);
        return _ret;
    }

    public R visit(CJumpStmt n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);
        String useTag = "use";
        n.f1.accept(this, (A) useTag);

        label_status = true;
        n.f2.accept(this, argu);
        label_status = false;

        String label_name = (String) n.f2.f0.tokenImage;
        current_block.jump_block_name = current_function.function_name + "_" + label_name;
        return _ret;
    }

    public R visit(JumpStmt n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);
        String label_name = (String) n.f1.f0.tokenImage;
        current_block.next_block = -1;
        current_block.jump_block_name = current_function.function_name + "_" + label_name;
        label_status = true;
        n.f1.accept(this, argu);
        label_status = false;
        return _ret;
    }

    public R visit(HStoreStmt n, A argu) {
        R _ret = null;
        String useTag = "use";
        n.f0.accept(this, argu);
        n.f1.accept(this, (A) useTag);
        n.f2.accept(this, argu);
        n.f3.accept(this, (A) useTag);
        return _ret;
    }

    public R visit(HLoadStmt n, A argu) {
        R _ret = null;
        String useTag = "use";
        String defTag = "def";
        n.f0.accept(this, argu);
        n.f1.accept(this, (A) defTag);
        n.f2.accept(this, (A) useTag);
        n.f3.accept(this, argu);
        return _ret;
    }
    public R visit(MoveStmt n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);
        String defTag = "def";
        n.f1.accept(this, (A) defTag);
        n.f2.accept(this, argu);
        return _ret;
    }

    public R visit(PrintStmt n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    public R visit(Exp n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);
        return _ret;
    }

    public R visit(StmtExp n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);

        current_block = new blockinfo(line_counter++);
        current_block.next_block = -1;
        current_function.blocks.add(current_block);

        n.f1.accept(this, argu);

        current_block = new blockinfo(line_counter++);
        current_function.blocks.add(current_block);

        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        current_block = new blockinfo(line_counter++);
        current_function.blocks.add(current_block);
        return _ret;
    }

    public R visit(Call n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);
        String useTag = "use";
        n.f1.accept(this, (A) useTag);
        n.f2.accept(this, argu);

        String argTag = "arg";
        argcounter = 0;
        n.f3.accept(this, (A) argTag);
        n.f4.accept(this, argu);

        current_function.max_callee_args = Math.max(current_function.max_callee_args, argcounter);
        current_function.call_check = true;
        return _ret;
    }

    public R visit(HAllocate n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    public R visit(BinOp n, A argu) {
        R _ret = null;
        String useTag = "use";
        n.f0.accept(this, argu);
        n.f1.accept(this, (A) useTag);
        n.f2.accept(this, argu);
        return _ret;
    }

    public R visit(Operator n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);
        return _ret;
    }

    public R visit(SimpleExp n, A argu) {
        R _ret = null;
        String useTag = "use";
        n.f0.accept(this, (A) useTag);
        return _ret;
    }

    public R visit(Temp n, A argu) {
        R _ret = null;

        n.f0.accept(this, argu);
        Integer temp_num = Integer.parseInt(n.f1.f0.tokenImage);

        if (argu.equals("def")) {
            current_block.def.add("TEMP " + temp_num.toString());
        } else if (argu.equals("use")) {
            current_block.use.add("TEMP " + temp_num.toString());
        } else if (argu.equals("arg")) {
            current_block.use.add("TEMP " + temp_num.toString());
            argcounter += 1;
        }

        return _ret;
    }

    public R visit(IntegerLiteral n, A argu) {
        R _ret = null;
        Integer literal_value = Integer.parseInt(n.f0.tokenImage);
        n.f0.accept(this, argu);
        return (R) literal_value;
    }

    public R visit(Label n, A argu) {
        R _ret = null;
        n.f0.accept(this, argu);

        String label_name = current_function.function_name + "_" + n.f0.tokenImage;
        if (argu == null) return _ret;

        if (argu.equals("label") && label_status == false) {
            current_function.label_map.put(label_name, line_counter);
            label_linemap.put(label_name, line_counter);
            current_block = new blockinfo(line_counter++);
            current_function.blocks.add(current_block);
        }
        return _ret;
    }

}
