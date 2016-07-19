/*
 * This file is part of EllyCommand.
 *
 * EllyCommand is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EllyCommand is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with EllyCommand.  If not, see <http://www.gnu.org/licenses/>.
 */
package ee.ellytr.command.exception;

import ee.ellytr.command.argument.ArgumentContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is used when a command sender attempts to execute a command, but there are no valid usages that match the sent
 * command.
 */
@Getter
@RequiredArgsConstructor
public class CommandUsageException extends CommandException {

  private final Error error;
  private final ArgumentContext invalidArgument;

  public enum Error {
    TOO_FEW_ARGUMENTS,
    TOO_MANY_ARGUMENTS,
    INVALID_ARGUMENTS,
    INVALID_USAGE
  }

}
