/*
 * Copyright (c) 2021, Bram91
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.modelexport;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("modelexport")
public interface ModelExportConfig extends Config {
	@ConfigItem(
			keyName = "material",
			name = "Export Color",
			description = "Also export colors for the models",
			position = 1
	)
	default  boolean material(){
		return  false;
	}
	@ConfigItem(
			keyName = "forceRestPose",
			name = "Force Rest Pose (Local Player)",
			description = "Forces local player to perform an animation on export. The resulting model is in a rest pose.",
			position = 2
	)
	default boolean forceRestPose() { return false; }

	@ConfigItem(
			position = 3,
			keyName = "regionLines",
			name = "Region Lines",
			description = "Show region (64 x 64) lines"
	)
	default boolean regionLines()
	{
		return true;
	}
	@ConfigItem(
			position = 4,
			keyName = "chunkSize",
			name = "Chunk Size",
			description = "The size of the chunk to show."
	)
	default int chunkSize()
	{
		return 16;
	}
}
