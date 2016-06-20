/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.client.file.options;

import alluxio.Configuration;
import alluxio.Constants;
import alluxio.annotation.PublicApi;
import alluxio.client.AlluxioStorageType;
import alluxio.client.ClientContext;
import alluxio.client.UnderStorageType;
import alluxio.client.WriteType;
import alluxio.client.file.policy.FileWriteLocationPolicy;
import alluxio.security.authorization.Permission;
import alluxio.util.CommonUtils;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;

import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Method options for writing a file.
 */
@PublicApi
@NotThreadSafe
public final class OutStreamOptions {
  private long mBlockSizeBytes;
  private long mTtl;
  private FileWriteLocationPolicy mLocationPolicy;
  private WriteType mWriteType;
  private Permission mPermission;

  /**
   * @return the default {@link OutStreamOptions}
   */
  public static OutStreamOptions defaults() {
    return new OutStreamOptions();
  }

  private OutStreamOptions() {
    Configuration conf = ClientContext.getConf();
    mBlockSizeBytes = conf.getBytes(Constants.USER_BLOCK_SIZE_BYTES_DEFAULT);
    mTtl = Constants.NO_TTL;
    try {
      mLocationPolicy =
          CommonUtils.createNewClassInstance(ClientContext.getConf()
              .<FileWriteLocationPolicy>getClass(Constants.USER_FILE_WRITE_LOCATION_POLICY),
              new Class[] {}, new Object[] {});
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
    mWriteType = conf.getEnum(Constants.USER_FILE_WRITE_TYPE_DEFAULT, WriteType.class);
    mPermission = Permission.defaults();
    try {
      // Set user and group from user login module, and apply default file UMask.
      mPermission.applyFileUMask(conf).setUserFromLoginModule(conf);
    } catch (IOException e) {
      // Fall through to system property approach
    }
  }

  /**
   * @return the block size
   */
  public long getBlockSizeBytes() {
    return mBlockSizeBytes;
  }

  /**
   * @return the file write location policy
   */
  public FileWriteLocationPolicy getLocationPolicy() {
    return mLocationPolicy;
  }

  /**
   * @return the Alluxio storage type
   */
  public AlluxioStorageType getAlluxioStorageType() {
    return mWriteType.getAlluxioStorageType();
  }

  /**
   * @return the TTL (time to live) value; it identifies duration (in milliseconds) the created file
   *         should be kept around before it is automatically deleted
   */
  public long getTtl() {
    return mTtl;
  }

  /**
   * @return the under storage type
   */
  public UnderStorageType getUnderStorageType() {
    return mWriteType.getUnderStorageType();
  }

  /**
   * @return the permission
   */
  public Permission getPermission() {
    return mPermission;
  }

  /**
   * Sets the size of the block in bytes.
   *
   * @param blockSizeBytes the block size to use
   * @return the updated options object
   */
  public OutStreamOptions setBlockSizeBytes(long blockSizeBytes) {
    mBlockSizeBytes = blockSizeBytes;
    return this;
  }

  /**
   * Sets the time to live.
   *
   * @param ttl the TTL (time to live) value to use; it identifies duration (in milliseconds) the
   *        created file should be kept around before it is automatically deleted, no matter
   *        whether the file is pinned
   * @return the updated options object
   */
  public OutStreamOptions setTtl(long ttl) {
    mTtl = ttl;
    return this;
  }

  /**
   * @param locationPolicy the file write location policy
   * @return the updated options object
   */
  public OutStreamOptions setLocationPolicy(FileWriteLocationPolicy locationPolicy) {
    mLocationPolicy = locationPolicy;
    return this;
  }

  /**
   * Sets the {@link WriteType}.
   *
   * @param writeType the {@link WriteType} to use for this operation. This will override both the
   *        {@link AlluxioStorageType} and {@link UnderStorageType}.
   * @return the updated options object
   */
  public OutStreamOptions setWriteType(WriteType writeType) {
    mWriteType = writeType;
    return this;
  }

  /**
   * Sets the {@link Permission}.
   *
   * @param perm the permission
   * @return the updated options object
   */
  public OutStreamOptions setPermission(Permission perm) {
    mPermission = perm;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OutStreamOptions)) {
      return false;
    }
    OutStreamOptions that = (OutStreamOptions) o;
    return Objects.equal(mBlockSizeBytes, that.mBlockSizeBytes)
        && Objects.equal(mTtl, that.mTtl)
        && Objects.equal(mLocationPolicy, that.mLocationPolicy)
        && Objects.equal(mWriteType, that.mWriteType)
        && Objects.equal(mPermission, that.mPermission);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mBlockSizeBytes, mTtl, mLocationPolicy, mWriteType, mPermission);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("blockSizeBytes", mBlockSizeBytes)
        .add("ttl", mTtl)
        .add("locationPolicy", mLocationPolicy)
        .add("writeType", mWriteType)
        .add("permission", mPermission)
        .toString();
  }
}
