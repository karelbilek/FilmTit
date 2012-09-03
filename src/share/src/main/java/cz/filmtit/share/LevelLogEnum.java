/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.share;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Enum for log levels in remote GUI logging
 */
public enum LevelLogEnum implements Comparable<LevelLogEnum>, Serializable, IsSerializable {
    Unknown,
    DebugNotice,
    Notice,
    Warning,
    Error;

    /**
     * Converts the log level to integer.
     * @param logLevel A log level
     * @return Integer representing the log level
     */
    public static int convertToInt(LevelLogEnum logLevel) {
          switch (logLevel) {
              case DebugNotice : return 0;
              case Notice: return 1;
              case Warning: return 2;
              case Error: return 3;
              default:
                  return -1;
          }
    }

    /**
     * Converts the log level to integer.
     * @return Integer representing the log level
     */
    public int convertToInt() {
          switch (this) {
              case DebugNotice : return 0;
              case Notice: return 1;
              case Warning: return 2;
              case Error: return 3;
              default:
                  return -1;
          }
    }

    /**
     * Converts an integer to a log level.
     * @param logLevelInt  Log level as an integer
     * @return Log level corresponding the given interger
     */
    public static LevelLogEnum convertTo(int logLevelInt){
        switch (logLevelInt){
            case 0 : return DebugNotice;
            case 1 : return Notice;
            case 2 : return Warning;
            case 3 : return Error;
            default:  return  Unknown;
        }
    }

    /**
     * Converts the log level to string.
     * @return  Text description
     */
    @Override
     public String toString() {
            switch (this) {
                case DebugNotice : return "Debug: ";
                case Notice: return "Notice: ";
                case Warning: return "Warning: ";
                case Error: return "Error: ";
                default:
                    return  "No level: " ;
            }

        }
    
}
