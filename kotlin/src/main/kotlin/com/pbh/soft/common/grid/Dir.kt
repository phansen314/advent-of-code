package com.pbh.soft.common.grid

enum class Dir(val dr: Int, val dc: Int) {
  NW(-1, -1), N(-1, 0), NE(-1, 1),
  W(0, -1), E(0, 1),
  SW(1, -1), S(1, 0), SE(1, 1);
}